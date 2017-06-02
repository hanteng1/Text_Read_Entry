package com.eschao.android.widget.pageflip.modify;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLUtils;

import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.PageFlipUtils;
import com.eschao.android.widget.pageflip.VertexProgram;
import com.eschao.android.widget.pageflip.Vertexes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by hanteng on 2017-06-02.
 * re-defining the page
 * only with first texture, and back texture
 * should support flip operation, as well as others
 * like pan, zoom, rotate etc
 *
 * note: the basic idea is each page is a single page...
 * not for representing the whole screen
 */

public class PageModify {

    private final static int TEXTURE_SIZE = 2;
    private final static int FIRST_TEXTURE_ID = 0;
    private final static int BACK_TEXTURE_ID = 1;
    private final static int INVALID_TEXTURE_ID = -1;

    private final static int[][] mPageApexOrders = new int[][] {
            new int[] {0, 1, 2, 3}, // for case A
            new int[] {1, 0, 3, 2}, // for case B
            new int[] {2, 3, 0, 1}, // for case C
            new int[] {3, 2, 1, 0}, // for case D
    };

    private final static int[][] mFoldVexOrders = new int[][] {
            new int[] {4, 3, 1, 2, 0}, // Case A
            new int[] {3, 3, 2, 0, 1}, // Case B
            new int[] {3, 2, 1, 3, 0}, // Case C
            new int[] {2, 2, 3, 1, 0}, // Case D
            new int[] {1, 0, 1, 3, 2}, // Case E
    };

    // page size
    public float left;
    public float right;
    public float top;
    public float bottom;
    public float width;
    public float height;

    // texture size for rendering page, normally they are same with page width
    // and height
    public float texWidth;
    public float texHeight;

    public GLPoint originP;
    public GLPoint diagonalP;

    private GLPoint mXFoldP;
    private GLPoint mYFoldP;

    // vertexes and texture coordinates buffer for full page
    private FloatBuffer mFullPageVexBuf;
    private FloatBuffer mFullPageTexCoordsBuf;

    // storing 4 apexes data of page
    private float[] mApexes;
    // texture coordinates for page apex
    private float[] mApexTexCoords;
    // vertex size of front of fold page and unfold page
    private int mFrontVertexSize;
    // index of apex order array for current original point
    private int mApexOrderIndex;

    // mask color of back texture
    float[][] maskColor;

    // texture(front, back and second) ids allocated by OpenGL
    private int[] mTexIDs;
    // unused texture ids, will be deleted when next OpenGL drawing
    private int[] mUnusedTexIDs;
    // actual size of mUnusedTexIDs
    private int mUnusedTexSize;

    /**
     * Constructor
     */
    public PageModify() {
        init(0, 0, 0, 0);
    }

    /**
     * Constructor with page size
     */
    public PageModify(float l, float r, float t, float b) {
        init(l, r, t, b);
    }

    private void init(float l, float r, float t, float b) {
        top = t;
        left = l;
        right = r;
        bottom = b;
        width = right - left;
        height = top - bottom;
        texWidth = width;
        texHeight = height;
        mFrontVertexSize = 0;
        mApexOrderIndex = 0;

        mXFoldP = new GLPoint();
        mYFoldP = new GLPoint();
        originP = new GLPoint();
        diagonalP = new GLPoint();

        maskColor = new float[][] {new float[] {0, 0, 0},
                new float[] {0, 0, 0}};

        mTexIDs = new int[] {INVALID_TEXTURE_ID,
                INVALID_TEXTURE_ID};
        mUnusedTexSize = 0;
        mUnusedTexIDs = new int[] {INVALID_TEXTURE_ID,
                INVALID_TEXTURE_ID};

        createVertexesBuffer();
        buildVertexesOfFullPage();
    }


    /**
     * Get page width
     *
     * @return page width
     */
    public float width() {
        return width;
    }

    /**
     * Gets page height
     *
     * @return page height
     */
    public float height() {
        return height;
    }

    /**
     * Is the first texture set?
     *
     * @return true if the first texture is set
     */
    public boolean isFirstTextureSet() {
        return mTexIDs[FIRST_TEXTURE_ID] != INVALID_TEXTURE_ID;
    }


    /**
     * Is the back texture set ?
     *
     * @return true if the back texture is set
     */
    public boolean isBackTextureSet() {
        return mTexIDs[BACK_TEXTURE_ID] != INVALID_TEXTURE_ID;
    }

    /**
     * Deletes unused texture ids
     * <p>It should be called in OpenGL thread</p>
     */
    public void deleteUnusedTextures() {
        if (mUnusedTexSize > 0) {
            glDeleteTextures(mUnusedTexSize, mUnusedTexIDs, 0);
            mUnusedTexSize = 0;
        }
    }

    /**
     * Recycle the first texture id
     * <p>Manually call this function to set the first texture with the second
     * one after page forward flipped over in single page mode.</p>
     *
     * @return self
     */
    public PageModify recycleFirstTexture() {
        if (mTexIDs[FIRST_TEXTURE_ID] > INVALID_TEXTURE_ID) {
            mUnusedTexIDs[mUnusedTexSize++] = mTexIDs[FIRST_TEXTURE_ID];
        }

        return this;
    }

    /**
     * Get back texture ID
     *
     * @return back texture id, If it is not set, return the first texture id
     */
    int getBackTextureID() {
        // In single page mode, the back texture is same with the first texture
        if (mTexIDs[BACK_TEXTURE_ID] == INVALID_TEXTURE_ID) {
            return mTexIDs[FIRST_TEXTURE_ID];
        }
        else {
            return mTexIDs[BACK_TEXTURE_ID];
        }
    }

    /**
     * Is given point(x, y) in page?
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the point is in page
     */
    public boolean contains(float x, float y) {
        return left < right && bottom < top &&
                left <= x && x < right &&
                bottom <= y && y < top;
    }

    /**
     * Is given x coordinate in specified page range?
     *
     * @param x x coordinate
     * @param ratio range ratio based on page width, start from OriginP.x
     * @return True if x is in specified range
     */
    public boolean isXInRange(float x, float ratio) {
        final float w = width * ratio;
        return originP.x < 0 ? x < (originP.x + w) : x > (originP.x - w);
    }

    /**
     * Is given x coordinate outside page width?
     *
     * @param x x coordinate
     * @return true if given x is not in page
     */
    public boolean isXOutsidePage(float x) {
        return originP.x < 0 ? x > diagonalP.x : x < diagonalP.x;
    }

    /**
     * Compute index of page apexes order for current original point
     */
    private void computeIndexOfApexOrder() {
        mApexOrderIndex = 0;
        if (originP.x < right && originP.y < 0) {
            mApexOrderIndex = 3;
        }
        else {
            if (originP.y > 0) {
                mApexOrderIndex++;
            }
            if (originP.x < right) {
                mApexOrderIndex++;
            }
        }
    }

    /**
     * Set original point and diagonal point
     *
     * @param dy relative finger movement on Y axis
     * @return self
     */
    public PageModify setOriginAndDiagonalPoints(float dy) {

        originP.x = right;
        diagonalP.x = left;

        if (dy > 0) {
            originP.y = bottom;
            diagonalP.y = top;
        }
        else {
            originP.y = top;
            diagonalP.y = bottom;
        }

        computeIndexOfApexOrder();

        // set texture coordinates
        originP.texX = (originP.x - left) / texWidth;
        originP.texY = (top - originP.y) / texHeight;
        diagonalP.texX = (diagonalP.x - left) / texWidth;
        diagonalP.texY = (top - diagonalP.y) / texHeight;
        return this;
    }

    /**
     * Invert Y coordinate of original point and diagonal point
     */
    public void invertYOfOriginPoint() {
        float t = originP.y;
        originP.y = diagonalP.y;
        diagonalP.y = t;

        t = originP.texY;
        originP.texY = diagonalP.texY;
        diagonalP.texY = t;

        // re-compute index for apex order since original point is changed
        computeIndexOfApexOrder();
    }

    /**
     * Compute X coordinate of texture
     *
     * @param x x coordinate
     * @return x coordinate of texture, value is in [0 .. 1]
     */
    public float textureX(float x) {
        return (x - left) / texWidth;
    }

    /**
     * Compute Y coordinate of texture
     *
     * @param y y coordinate
     * @return y coordinate of texture, value is in [0 .. 1]
     */
    public float textureY(float y) {
        return (top - y) / texHeight;
    }

    /**
     * Delete all textures
     */
    public void deleteAllTextures() {
        glDeleteTextures(TEXTURE_SIZE, mTexIDs, 0);
        mTexIDs[FIRST_TEXTURE_ID] = INVALID_TEXTURE_ID;
        mTexIDs[BACK_TEXTURE_ID] = INVALID_TEXTURE_ID;
    }

    /**
     * Set the first texture with given bitmap
     *
     * @param b Bitmap object for creating texture
     */
    public void setFirstTexture(Bitmap b) {
        // compute mask color
        int color = PageFlipUtils.computeAverageColor(b, 30);
        maskColor[FIRST_TEXTURE_ID][0] = Color.red(color) / 255.0f;
        maskColor[FIRST_TEXTURE_ID][1] = Color.green(color) / 255.0f;
        maskColor[FIRST_TEXTURE_ID][2] = Color.blue(color) / 255.0f;

        glGenTextures(1, mTexIDs, FIRST_TEXTURE_ID);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTexIDs[FIRST_TEXTURE_ID]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, b, 0);
    }

    /**
     * Set the back texture with given bitmap
     * <p>If given bitmap is null, the back texture will be same with the first
     * texture</p>
     *
     * @param b Bitmap object for creating back texture
     */
    public void setBackTexture(Bitmap b) {
        if (b == null) {
            // back texture is same with the first texture
            if (mTexIDs[BACK_TEXTURE_ID] != INVALID_TEXTURE_ID) {
                mUnusedTexIDs[mUnusedTexSize++] = mTexIDs[BACK_TEXTURE_ID];
            }
            mTexIDs[BACK_TEXTURE_ID] = INVALID_TEXTURE_ID;
        }
        else {
            // compute mask color
            int color = PageFlipUtils.computeAverageColor(b, 50);
            maskColor[BACK_TEXTURE_ID][0] = Color.red(color) / 255.0f;
            maskColor[BACK_TEXTURE_ID][1] = Color.green(color) / 255.0f;
            maskColor[BACK_TEXTURE_ID][2] = Color.blue(color) / 255.0f;

            glGenTextures(1, mTexIDs, BACK_TEXTURE_ID);
            glBindTexture(GL_TEXTURE_2D, mTexIDs[BACK_TEXTURE_ID]);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, b, 0);
        }
    }

    /**
     * Draw front page when page is flipping
     *
     * @param program GL shader program
     * @param vertexes Vertexes of the curled front page
     */
    public void drawFrontPage(VertexProgram program,
                              Vertexes vertexes) {
        // 1. draw unfold part and curled part with the first texture
        glUniformMatrix4fv(program.mMVPMatrixLoc, 1, false,
                VertexProgram.MVPMatrix, 0);
        glBindTexture(GL_TEXTURE_2D, mTexIDs[FIRST_TEXTURE_ID]);
        glUniform1i(program.mTextureLoc, 0);
        vertexes.drawWith(GL_TRIANGLE_STRIP,
                program.mVertexPosLoc,
                program.mTexCoordLoc,
                0, mFrontVertexSize);

    }

    /**
     * Draw full page
     *
     * @param program GL shader program
     */
    public void drawFullPage(VertexProgram program) {

        drawFullPage(program, mTexIDs[FIRST_TEXTURE_ID]);

    }

    /**
     * Draw full page with given texture id
     */
    private void drawFullPage(VertexProgram program, int textureID) {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(program.mTextureLoc, 0);

        glVertexAttribPointer(program.mVertexPosLoc, 3, GL_FLOAT, false, 0,
                mFullPageVexBuf);
        glEnableVertexAttribArray(program.mVertexPosLoc);

        glVertexAttribPointer(program.mTexCoordLoc, 2, GL_FLOAT, false, 0,
                mFullPageTexCoordsBuf);
        glEnableVertexAttribArray(program.mTexCoordLoc);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    /**
     * Create vertexes buffer
     */
    private void createVertexesBuffer() {
        // 4 vertexes for full page
        mFullPageVexBuf = ByteBuffer.allocateDirect(48)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mFullPageTexCoordsBuf = ByteBuffer.allocateDirect(32)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mApexes = new float[12];
        mApexTexCoords = new float[8];
    }

    /**
     * Build vertexes of page when page is flipping vertically
     * <pre>
     *        <---- flip
     *     1        fY    2
     *     +--------#-----+
     *     |        |     |
     *     |        |     |
     *     |        |     |
     *     +--------#-----+
     *     4        fX    3
     * </pre>
     * <p>
     * There is only one case to draw when page is flipping vertically
     * will use this
     * </p>
     * <ul>
     *      <li>Page is flipping from right -> left</li>
     *      <li>Origin point: 3</li>
     *      <li>Diagonal point: 1</li>
     *      <li>xFoldP1.y: fY, xFoldP2.x: fX</li>
     *      <li>Drawing front part with the first texture(GL_TRIANGLE_STRIP):
     *      fX -> fY -> 4 -> 1</li>
     *      <li>Drawing back part with the second texture(GL_TRIANGLE_STRIP):
     *      3 -> 2 -> fX -> fY</li>
     * </ul>
     *
     * @param frontVertexes vertexes for drawing font part of page
     * @param xFoldP1 fold point on X axis
     */
    public void buildVertexesOfPageWhenVertical(Vertexes frontVertexes,
                                                PointF xFoldP1) {
        // if xFoldX and yFoldY are both outside the page, use the last vertex
        // order to draw page
        int index = 4;

        // compute xFoldX and yFoldY points
        if (!isXOutsidePage(xFoldP1.x)) {
            // use the case B of vertex order to draw page
            index = 1;
            float cx = textureX(xFoldP1.x);
            mXFoldP.set(xFoldP1.x, originP.y, 0, cx, originP.texY);
            mYFoldP.set(xFoldP1.x, diagonalP.y, 0, cx, diagonalP.texY);
        }

        // get apex order and fold vertex order
        final int[] apexOrder = mPageApexOrders[mApexOrderIndex];
        final int[] vexOrder = mFoldVexOrders[index];

        // need to draw first texture, add xFoldX and yFoldY first. Remember
        // the adding order of vertex in float buffer is X point prior to Y
        // point
        if (vexOrder[0] > 1) {
            frontVertexes.addVertex(mXFoldP).addVertex(mYFoldP);
        }

        // add the leftover vertexes for the first texture
        for (int i = 1; i < vexOrder[0]; ++i) {
            int k = apexOrder[vexOrder[i]];
            int m = k * 3;
            int n = k << 1;
            frontVertexes.addVertex(mApexes[m], mApexes[m + 1], 0,
                    mApexTexCoords[n], mApexTexCoords[n + 1]);
        }

        // the vertex size for drawing front of fold page and first texture
        mFrontVertexSize = frontVertexes.mNext / 3;

        // if xFoldX and yFoldY are in the page, need add them for drawing the
        // second texture
        if (vexOrder[0] > 1) {
            mXFoldP.z = mYFoldP.z = -1;
            frontVertexes.addVertex(mXFoldP).addVertex(mYFoldP);
        }

        // add the remaining vertexes for the second texture
        for (int i = vexOrder[0]; i < vexOrder.length; ++i) {
            int k = apexOrder[vexOrder[i]];
            int m = k * 3;
            int n = k << 1;
            frontVertexes.addVertex(mApexes[m], mApexes[m + 1], -1,
                    mApexTexCoords[n], mApexTexCoords[n + 1]);
        }
    }

    /**
     * Build vertexes of page when page flip is slope
     * <p>See {@link #mApexOrderIndex} and {@link #mFoldVexOrders} to get more
     * details</p>
     *
     * @param frontVertexes vertexes for drawing front part of page
     * @param xFoldP1 fold point on X axis
     * @param yFoldP1 fold point on Y axis
     * @param kValue tan value of page curling angle
     */
    public void buildVertexesOfPageWhenSlope(Vertexes frontVertexes,
                                             PointF xFoldP1,
                                             PointF yFoldP1,
                                             float kValue) {
        // compute xFoldX point
        float halfH = height * 0.5f;
        int index = 0;
        mXFoldP.set(xFoldP1.x, originP.y, 0, textureX(xFoldP1.x), originP.texY);
        if (isXOutsidePage(xFoldP1.x)) {
            index = 2;
            mXFoldP.x = diagonalP.x;
            mXFoldP.y = originP.y + (xFoldP1.x - diagonalP.x) / kValue;
            mXFoldP.texX = diagonalP.texX;
            mXFoldP.texY = textureY(mXFoldP.y);
        }

        // compute yFoldY point
        mYFoldP.set(originP.x, yFoldP1.y, 0, originP.texX, textureY(yFoldP1.y));
        if (Math.abs(yFoldP1.y) > halfH)  {
            index++;
            mYFoldP.x = originP.x + kValue * (yFoldP1.y - diagonalP.y);
            if (isXOutsidePage(mYFoldP.x)) {
                index++;
            }
            else {
                mYFoldP.y = diagonalP.y;
                mYFoldP.texX = textureX(mYFoldP.x);
                mYFoldP.texY = diagonalP.texY;
            }
        }

        // get apex order and fold vertex order
        final int[] apexOrder = mPageApexOrders[mApexOrderIndex];
        final int[] vexOrder = mFoldVexOrders[index];

        // need to draw first texture, add xFoldX and yFoldY first. Remember
        // the adding order of vertex in float buffer is X point prior to Y
        // point
        if (vexOrder[0] > 1) {
            frontVertexes.addVertex(mXFoldP).addVertex(mYFoldP);
        }

        // add the leftover vertexes for the first texture
        for (int i = 1; i < vexOrder[0]; ++i) {
            int k = apexOrder[vexOrder[i]];
            int m = k * 3;
            int n = k << 1;
            frontVertexes.addVertex(mApexes[m], mApexes[m + 1], 0,
                    mApexTexCoords[n], mApexTexCoords[n + 1]);
        }

        // the vertex size for drawing front of fold page and first texture
        mFrontVertexSize = frontVertexes.mNext / 3;

        // if xFoldX and yFoldY are in the page, need add them for drawing the
        // second texture
        if (vexOrder[0] > 1) {
            mXFoldP.z = mYFoldP.z = -1;
            frontVertexes.addVertex(mXFoldP).addVertex(mYFoldP);
        }

        // add the remaining vertexes for the second texture
        for (int i = vexOrder[0]; i < vexOrder.length; ++i) {
            int k = apexOrder[vexOrder[i]];
            int m = k * 3;
            int n = k << 1;
            frontVertexes.addVertex(mApexes[m], mApexes[m + 1], -1,
                    mApexTexCoords[n], mApexTexCoords[n + 1]);
        }
    }

    /**
     * Build vertexes of full page
     * <pre>
     *        <---- flip
     *     3              2
     *     +--------------+
     *     |              |
     *     |              |
     *     |              |
     *     |              |
     *     +--------------+
     *     4              1
     * </pre>
     * <ul>
     *      <li>Page is flipping from right -> left</li>
     *      <li>Origin point: 3</li>
     *      <li>Diagonal point: 1</li>
     *      <li>xFoldP1.y: fY, xFoldP2.x: fX</li>
     *      <li>Drawing order: 3 -> 2 -> 4 -> 1</li>
     * </ul>
     */
    private void buildVertexesOfFullPage() {
        int i = 0;
        int j = 0;

        mApexes[i++] = right;
        mApexes[i++] = bottom;
        mApexes[i++] = 0;
        mApexTexCoords[j++] = textureX(right);
        mApexTexCoords[j++] = textureY(bottom);

        mApexes[i++] = right;
        mApexes[i++] = top;
        mApexes[i++] = 0;
        mApexTexCoords[j++] = textureX(right);
        mApexTexCoords[j++] = textureY(top);

        mApexes[i++] = left;
        mApexes[i++] = top;
        mApexes[i++] = 0;
        mApexTexCoords[j++] = textureX(left);
        mApexTexCoords[j++] = textureY(top);

        mApexes[i++] = left;
        mApexes[i++] = bottom;
        mApexes[i] = 0;
        mApexTexCoords[j++] = textureX(left);
        mApexTexCoords[j] = textureY(bottom);

        mFullPageVexBuf.put(mApexes, 0, 12).position(0);
        mFullPageTexCoordsBuf.put(mApexTexCoords, 0, 8).position(0);
    }
}
