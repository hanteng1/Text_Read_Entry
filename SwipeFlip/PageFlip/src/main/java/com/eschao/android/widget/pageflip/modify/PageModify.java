package com.eschao.android.widget.pageflip.modify;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLUtils;
import android.util.Log;

import com.eschao.android.widget.pageflip.FoldBackVertexProgram;
import com.eschao.android.widget.pageflip.FoldBackVertexes;
import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.GLViewRect;
import com.eschao.android.widget.pageflip.PageFlipUtils;
import com.eschao.android.widget.pageflip.ShadowVertexProgram;
import com.eschao.android.widget.pageflip.ShadowVertexes;
import com.eschao.android.widget.pageflip.ShadowWidth;
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
    private final static int FRONT_TEXTURE_ID = 0;
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
    public float[][] maskColor;

    // texture(front, back and second) ids allocated by OpenGL
    private int[] mTexIDs;
    // unused texture ids, will be deleted when next OpenGL drawing
    private int[] mUnusedTexIDs;
    // actual size of mUnusedTexIDs
    private int mUnusedTexSize;

    //position and state control variables
    public VertexProgram mVertexProgram;


    //computing vertexes
    private PointF mMiddleP;
    private PointF mYFoldPc;
    private PointF mYFoldP0c;
    private PointF mYFoldP1c;
    private PointF mXFoldPc;
    private PointF mXFoldP0c;
    private PointF mXFoldP1c;


    //translation with x
    public float mTransOffX;
    private float mMaxT2OAngleTan;
    private float mMaxT2DAngleTan;
    private float mKValue;
    private float mLenOfTouchOrigin;
    private float mR;
    private float mSemiPerimeterRatio;
    private int mMeshCount;

    private ShadowWidth mFoldEdgesShadowWidth;
    private ShadowWidth mFoldBaseShadowWidth;

    public Vertexes mFoldFrontVertexes;
    public FoldBackVertexes mFoldBackVertexes;
    public ShadowVertexes mFoldEdgesShadow;
    public ShadowVertexes mFoldBaseShadow;

    public FoldBackVertexProgram mFoldBackVertexProgram;
    public ShadowVertexProgram mShadowVertexProgram;


    private final static int FOLD_TOP_EDGE_SHADOW_VEX_COUNT = 22;

    // fold edge shadow color
    private final static float FOLD_EDGE_SHADOW_START_COLOR = 0.1f;
    private final static float FOLD_EDGE_SHADOW_START_ALPHA = 0.25f;
    private final static float FOLD_EDGE_SHADOW_END_COLOR = 0.3f;
    private final static float FOLD_EDGE_SHADOW_END_ALPHA = 0f;

    // fold base shadow color
    private final static float FOLD_BASE_SHADOW_START_COLOR = 0.05f;
    private final static float FOLD_BASE_SHADOW_START_ALPHA = 0.4f;
    private final static float FOLD_BASE_SHADOW_END_COLOR = 0.3f;
    private final static float FOLD_BASE_SHADOW_END_ALPHA = 0f;

    private final static int DEFAULT_MESH_VERTEX_PIXELS = 10;
    private final static int MESH_COUNT_THRESHOLD = 20;

    // The min page curl angle (5 degree)
    private final static int MIN_PAGE_CURL_ANGLE = 5;
    // The max page curl angle (5 degree)
    private final static int MAX_PAGE_CURL_ANGLE = 65;
    private final static int PAGE_CURL_ANGEL_DIFF = MAX_PAGE_CURL_ANGLE -
            MIN_PAGE_CURL_ANGLE;
    private final static float MIN_PAGE_CURL_RADIAN =
            (float)(Math.PI * MIN_PAGE_CURL_ANGLE / 180);
    private final static float MAX_PAGE_CURL_RADIAN=
            (float)(Math.PI * MAX_PAGE_CURL_ANGLE / 180);
    private final static float MIN_PAGE_CURL_TAN_OF_ANGLE =
            (float)Math.tan(MIN_PAGE_CURL_RADIAN);
    private final static float MAX_PAGE_CURL_TAN_OF_ANGEL =
            (float)Math.tan(MAX_PAGE_CURL_RADIAN);
    private final static float MAX_PAGE_CURL_ANGLE_RATIO =
            MAX_PAGE_CURL_ANGLE / 90f;
    private final static float MAX_TAN_OF_FORWARD_FLIP =
            (float)Math.tan(Math.PI / 6);
    private final static float MAX_TAN_OF_BACKWARD_FLIP =
            (float)Math.tan(Math.PI / 20);


    //view rect
    public GLViewRect mViewRect;
    public boolean mIsVertical;
    public int mPixelsOfMesh;

    public PointF mFakeTouchP;

    private static final String TAG = "PageModify";

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


        //initialization
        mSemiPerimeterRatio = 0.8f;
        mTransOffX = 0.0f;
        mFakeTouchP = new PointF();

        mMiddleP = new PointF();
        mYFoldPc = new PointF();
        mYFoldP0c = new PointF();
        mYFoldP1c = new PointF();
        mXFoldPc = new PointF();
        mXFoldP0c = new PointF();
        mXFoldP1c = new PointF();

        mViewRect = new GLViewRect();
        mIsVertical = false;
        mPixelsOfMesh = DEFAULT_MESH_VERTEX_PIXELS;

        mFoldEdgesShadowWidth = new ShadowWidth(5, 30, 0.25f);
        mFoldBaseShadowWidth = new ShadowWidth(2, 40, 0.4f);

        mVertexProgram = new VertexProgram();
        mFoldBackVertexProgram = new FoldBackVertexProgram();
        mShadowVertexProgram = new ShadowVertexProgram();

        mFoldFrontVertexes = new Vertexes();
        mFoldBackVertexes = new FoldBackVertexes();
        mFoldEdgesShadow = new ShadowVertexes(FOLD_TOP_EDGE_SHADOW_VEX_COUNT,
                FOLD_EDGE_SHADOW_START_COLOR,
                FOLD_EDGE_SHADOW_START_ALPHA,
                FOLD_EDGE_SHADOW_END_COLOR,
                FOLD_EDGE_SHADOW_END_ALPHA);
        mFoldBaseShadow = new ShadowVertexes(0,
                FOLD_BASE_SHADOW_START_COLOR,
                FOLD_BASE_SHADOW_START_ALPHA,
                FOLD_BASE_SHADOW_END_COLOR,
                FOLD_BASE_SHADOW_END_ALPHA);


        mSemiPerimeterRatio = 0.8f;
        mFoldEdgesShadowWidth.set(5, 60, 0.3f);
        mFoldBaseShadowWidth.set(5, 80, 0.4f);

        mMaxT2OAngleTan = 0f;
        mMaxT2DAngleTan = 0f;

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
    public boolean isFrontTextureSet() {
        return mTexIDs[FRONT_TEXTURE_ID] != INVALID_TEXTURE_ID;
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
    public PageModify recycleFrontTexture() {
        if (mTexIDs[FRONT_TEXTURE_ID] > INVALID_TEXTURE_ID) {
            mUnusedTexIDs[mUnusedTexSize++] = mTexIDs[FRONT_TEXTURE_ID];
        }

        return this;
    }

    /**
     * Get back texture ID
     *
     * @return back texture id, If it is not set, return the first texture id
     */
    public int getBackTextureID() {
        // In single page mode, the back texture is same with the first texture
        if (mTexIDs[BACK_TEXTURE_ID] == INVALID_TEXTURE_ID) {
            return mTexIDs[FRONT_TEXTURE_ID];
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
        mTexIDs[FRONT_TEXTURE_ID] = INVALID_TEXTURE_ID;
        mTexIDs[BACK_TEXTURE_ID] = INVALID_TEXTURE_ID;
    }

    /**
     * Set the first texture with given bitmap
     *
     * @param b Bitmap object for creating texture
     */
    public void setFrontTexture(Bitmap b) {
        // compute mask color
        int color = PageFlipUtils.computeAverageColor(b, 30);
        maskColor[FRONT_TEXTURE_ID][0] = Color.red(color) / 255.0f;
        maskColor[FRONT_TEXTURE_ID][1] = Color.green(color) / 255.0f;
        maskColor[FRONT_TEXTURE_ID][2] = Color.blue(color) / 255.0f;

        glGenTextures(1, mTexIDs, FRONT_TEXTURE_ID);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTexIDs[FRONT_TEXTURE_ID]);
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
        glBindTexture(GL_TEXTURE_2D, mTexIDs[FRONT_TEXTURE_ID]);
        glUniform1i(program.mTextureLoc, 0);
        vertexes.drawWith(GL_TRIANGLE_STRIP,
                program.mVertexPosLoc,
                program.mTexCoordLoc,
                0, mFrontVertexSize);

    }

    public void drawFullPage()
    {
        drawFullPage(mVertexProgram);
    }

    /**
     * Draw full page
     *
     * @param program GL shader program
     */
    public void drawFullPage(VertexProgram program) {

        drawFullPage(program, mTexIDs[FRONT_TEXTURE_ID]);
    }

    /**
     * Draw full page with given texture id
     */
    public void drawFullPage(VertexProgram program, int textureID) {
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




    //===========================================




    /**
     * Compute key vertexes when page flip is vertical
     */
    public void computeKeyVertexesWhenVertical() {
        final float oX = originP.x ;
        final float oY = originP.y;
        final float dY = diagonalP.y;

        mFakeTouchP.y = oY;
        mMiddleP.y = oY;

        // set key point on X axis
        float r0 = 1 - mSemiPerimeterRatio;
        float r1 = 1 + mSemiPerimeterRatio;
        mXFoldPc.set(mMiddleP.x, oY);
        mXFoldP0c.set(oX + (mXFoldPc.x - oX) * r0, mXFoldPc.y);
        mXFoldP1c.set(oX + r1 * (mXFoldPc.x - oX), mXFoldPc.y);

        // set key point on Y axis
        mYFoldPc.set(mMiddleP.x, dY);
        mYFoldP0c.set(mXFoldP0c.x, mYFoldPc.y);
        mYFoldP1c.set(mXFoldP1c.x, mYFoldPc.y);

        // line length from mTouchP to originP
        mLenOfTouchOrigin = Math.abs(mFakeTouchP.x - oX);
        mR = (float)(mLenOfTouchOrigin * mSemiPerimeterRatio / Math.PI);

        // compute mesh count
        computeMeshCount();
    }

    /**
     * Compute all vertexes when page flip is vertical
     */
    public void computeVertexesWhenVertical() {
        float x = mMiddleP.x;
        float stepX = (mMiddleP.x - mXFoldP0c.x) / mMeshCount;

        final PageModify page = this;
        final float oY = page.originP.y;
        final float dY = page.diagonalP.y;
        final float cDY = page.diagonalP.texY;
        final float cOY = page.originP.texY;
        final float cOX = page.originP.texX;

        // compute the point on back page half cylinder
        mFoldBackVertexes.reset();

        for (int i = 0; i <= mMeshCount; ++i, x -= stepX) {
            // compute radian of x point
            float x2t = x - mXFoldP1c.x;
            float radius = x2t / mR;
            float sinR = (float)Math.sin(radius);
            float coordX = page.textureX(x);
            float fx = mXFoldP1c.x + mR * sinR;
            float fz = (float) (mR * (1 - Math.cos(radius)));

            // compute vertex when it is curled
            mFoldBackVertexes.addVertex(fx, dY, fz, sinR, coordX, cDY)
                    .addVertex(fx, oY, fz, sinR, coordX, cOY);
        }

        float tx0 = mFakeTouchP.x;
        mFoldBackVertexes.addVertex(tx0, dY, 1, 0, cOX, cDY)
                .addVertex(tx0, oY, 1, 0, cOX, cOY)
                .toFloatBuffer();

        // compute shadow width
        float sw = -mFoldEdgesShadowWidth.width(mR);
        float bw = mFoldBaseShadowWidth.width(mR);
        if (page.originP.x < 0) {
            sw = -sw;
            bw = -bw;
        }

        // fold base shadow
        float bx0 = mFoldBackVertexes.mVertexes[0];
        mFoldBaseShadow.setVertexes(0, bx0, oY, bx0 + bw, oY)
                .setVertexes(8, bx0, dY, bx0 + bw, dY)
                .toFloatBuffer(16);

        // fold edge shadow
        mFoldEdgesShadow.setVertexes(0, tx0, oY, tx0 + sw, oY)
                .setVertexes(8, tx0, dY, tx0 + sw, dY)
                .toFloatBuffer(16);

        // fold front
        mFoldFrontVertexes.reset();
        page.buildVertexesOfPageWhenVertical(mFoldFrontVertexes, mXFoldP1c);
        mFoldFrontVertexes.toFloatBuffer();
    }

    /**
     * Compute key vertexes when page flip is slope
     */
    public void computeKeyVertexesWhenSlope() {
        final float oX = originP.x;
        final float oY = originP.y;

        float dX = mMiddleP.x - oX;
        float dY = mMiddleP.y - oY;

        // compute key points on X axis
        float r0 = 1 - mSemiPerimeterRatio;
        float r1 = 1 + mSemiPerimeterRatio;
        mXFoldPc.set(mMiddleP.x + dY * dY / dX, oY);
        mXFoldP0c.set(oX + (mXFoldP.x - oX) * r0, mXFoldP.y);
        mXFoldP1c.set(oX + r1 * (mXFoldP.x - oX), mXFoldP.y);

        // compute key points on Y axis
        mYFoldPc.set(oX, mMiddleP.y + dX * dX / dY);
        mYFoldP0c.set(mYFoldP.x, oY + (mYFoldP.y - oY) * r0);
        mYFoldP1c.set(mYFoldP.x, oY + r1 * (mYFoldP.y - oY));

        // line length from TouchXY to OriginalXY
        mLenOfTouchOrigin = (float)Math.hypot((mFakeTouchP.x - oX),
                (mFakeTouchP.y - oY));

        // cylinder radius
        mR = (float)(mLenOfTouchOrigin * mSemiPerimeterRatio / Math.PI);

        // compute line slope
        mKValue = (mFakeTouchP.y - oY) / (mFakeTouchP.x - oX);

        // compute mesh count
        computeMeshCount();
    }

    /**
     * Compute back vertex and edge shadow vertex of fold page
     * <p>
     * In 2D coordinate system, for every vertex on fold page, we will follow
     * the below steps to compute its 3D point (x,y,z) on curled page(cylinder):
     * </p>
     * <ul>
     *     <li>deem originP as (0, 0) to simplify the next computing steps</li>
     *     <li>translate point(x, y) to new coordinate system
     *     (originP is (0, 0))</li>
     *     <li>rotate point(x, y) with curling angle A in clockwise</li>
     *     <li>compute 3d point (x, y, z) for 2d point(x, y), at this time, the
     *     cylinder is vertical in new coordinate system which will help us
     *     compute point</li>
     *     <li>rotate 3d point (x, y, z) with -A to restore</li>
     *     <li>translate 3d point (x, y, z) to original coordinate system</li>
     * </ul>
     *
     * <p>For point of edge shadow, the most computing steps are same but:</p>
     * <ul>
     *     <li>shadow point is following the page point except different x
     *     coordinate</li>
     *     <li>shadow point has same z coordinate with the page point</li>
     * </ul>
     *
     * @param isX is vertex for x point on x axis or y point on y axis?
     * @param x0 x of point on axis
     * @param y0 y of point on axis
     * @param sx0 x of edge shadow point
     * @param sy0 y of edge shadow point
     * @param tX x of xFoldP1 point in rotated coordinate system
     * @param sinA sin value of page curling angle
     * @param cosA cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX x of originate point
     * @param oY y of originate point
     */
    public void computeBackVertex(boolean isX, float x0, float y0, float sx0,
                                   float sy0, float tX, float sinA, float cosA,
                                   float coordX, float coordY, float oX,
                                   float oY) {
        // rotate degree A
        float x = x0 * cosA - y0 * sinA;
        float y = x0 * sinA + y0 * cosA;

        // rotate degree A for vertexes of fold edge shadow
        float sx = sx0 * cosA - sy0 * sinA;
        float sy = sx0 * sinA + sy0 * cosA;

        // compute mapping point on cylinder
        float rad = (x - tX) / mR;
        double sinR = Math.sin(rad);
        x = (float) (tX + mR * sinR);
        float cz = (float) (mR * (1 - Math.cos(rad)));

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        float cx = x * cosA + y * sinA + oX;
        float cy = y * cosA - x * sinA + oY;
        mFoldBackVertexes.addVertex(cx, cy, cz, (float)sinR, coordX, coordY);

        // compute coordinates of fold shadow edge
        float sRadian = (sx - tX) / mR;
        sx = (float)(tX + mR * Math.sin(sRadian));
        mFoldEdgesShadow.addVertexes(isX, cx, cy,
                sx * cosA + sy * sinA + oX,
                sy * cosA - sx * sinA + oY);
    }

    /**
     * Compute back vertex of fold page
     * <p>
     * Almost same with another computeBackVertex function except expunging the
     * shadow point part
     * </p>
     *
     * @param x0 x of point on axis
     * @param y0 y of point on axis
     * @param tX x of xFoldP1 point in rotated coordinate system
     * @param sinA sin value of page curling angle
     * @param cosA cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX x of originate point
     * @param oY y of originate point
     */
    public void computeBackVertex(float x0, float y0, float tX,
                                   float sinA, float cosA, float coordX,
                                   float coordY, float oX, float oY) {
        // rotate degree A
        float x = x0 * cosA - y0 * sinA;
        float y = x0 * sinA + y0 * cosA;

        // compute mapping point on cylinder
        float rad = (x - tX) / mR;
        double sinR = Math.sin(rad);
        x = (float) (tX + mR * sinR);
        float cz = (float) (mR * (1 - Math.cos(rad)));

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        float cx = x * cosA + y * sinA + oX;
        float cy = y * cosA - x * sinA + oY;
        mFoldBackVertexes.addVertex(cx, cy, cz, (float)sinR, coordX, coordY);
    }

    /**
     * Compute front vertex and base shadow vertex of fold page
     * <p>The computing principle is almost same with
     * {@link #computeBackVertex(boolean, float, float, float, float, float,
     * float, float, float, float, float, float)}</p>
     *
     * @param isX is vertex for x point on x axis or y point on y axis?
     * @param x0 x of point on axis
     * @param y0 y of point on axis
     * @param tX x of xFoldP1 point in rotated coordinate system
     * @param sinA sin value of page curling angle
     * @param cosA cos value of page curling angel
     * @param baseWcosA base shadow width * cosA
     * @param baseWsinA base shadow width * sinA
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX x of originate point
     * @param oY y of originate point
     */
    public void computeFrontVertex(boolean isX, float x0, float y0, float tX,
                                    float sinA, float cosA,
                                    float baseWcosA, float baseWsinA,
                                    float coordX, float coordY,
                                    float oX, float oY, float dY) {
        // rotate degree A
        float x = x0 * cosA - y0 * sinA;
        float y = x0 * sinA + y0 * cosA;

        // compute mapping point on cylinder
        float rad = (x - tX)/ mR;
        x = (float)(tX + mR * Math.sin(rad));
        float cz = (float)(mR * (1 - Math.cos(rad)));

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        float cx = x * cosA + y * sinA + oX;
        float cy = y * cosA - x * sinA + oY;
        mFoldFrontVertexes.addVertex(cx, cy, cz, coordX, coordY);
        mFoldBaseShadow.addVertexes(isX, cx, cy,
                cx + baseWcosA, cy - baseWsinA);
    }

    /**
     * Compute front vertex
     * <p>The difference with another
     * {@link #computeFrontVertex(boolean, float, float, float, float, float,
     * float, float, float, float, float, float, float)} is that it won't
     * compute base shadow vertex</p>
     *
     * @param x0 x of point on axis
     * @param y0 y of point on axis
     * @param tX x of xFoldP1 point in rotated coordinate system
     * @param sinA sin value of page curling angle
     * @param cosA cos value of page curling angel
     * @param coordX x of texture coordinate
     * @param coordY y of texture coordinate
     * @param oX x of originate point
     * @param oY y of originate point
     */
    public void computeFrontVertex(float x0, float y0, float tX,
                                    float sinA, float cosA,
                                    float coordX, float coordY,
                                    float oX, float oY) {
        // rotate degree A
        float x = x0 * cosA - y0 * sinA;
        float y = x0 * sinA + y0 * cosA;

        // compute mapping point on cylinder
        float rad = (x - tX)/ mR;
        x = (float)(tX + mR * Math.sin(rad));
        float cz = (float)(mR * (1 - Math.cos(rad)));

        // rotate degree -A, sin(-A) = -sin(A), cos(-A) = cos(A)
        float cx = x * cosA + y * sinA + oX;
        float cy = y * cosA - x * sinA + oY;
        mFoldFrontVertexes.addVertex(cx, cy, cz, coordX, coordY);
    }

    /**
     * Compute last vertex of base shadow(backward direction)
     * <p>
     * The vertexes of base shadow are composed by two part: forward and
     * backward part. Forward vertexes are computed from XFold points and
     * backward vertexes are computed from YFold points. The reason why we use
     * forward and backward is because how to change float buffer index when we
     * add a new vertex to buffer. Backward means the index is declined from
     * buffer middle position to the head, in contrast, the forward is
     * increasing index from middle to the tail. This design will help keep
     * float buffer consecutive and to be draw at a time.
     * </p><p>
     * Sometimes, the whole or part of YFold points will be outside page, that
     * means their Y coordinate are greater than page height(diagonal.y). In
     * this case, we have to crop them like cropping line on 2D coordinate
     * system. If delve further, we can conclude that we only need to compute
     * the first start/end vertexes which is falling on the border line of
     * diagonal.y since other backward vertexes must be outside page and could
     * not be seen, and then combine these vertexes with forward vertexes to
     * render base shadow.
     * </p><p>
     * This function is just used to compute the couple vertexes.
     * </p>
     *
     * @param x0 x of point on axis
     * @param y0 y of point on axis
     * @param tX x of xFoldP1 point in rotated coordinate system
     * @param sinA sin value of page curling angle
     * @param cosA cos value of page curling angel
     * @param baseWcosA base shadow width * cosA
     * @param baseWsinA base shadow width * sinA
     * @param oX x of originate point
     * @param oY y of originate point
     * @param dY y of diagonal point
     */
    public void computeBaseShadowLastVertex(float x0, float y0, float tX,
                                             float sinA, float cosA,
                                             float baseWcosA, float baseWsinA,
                                             float oX, float oY, float dY) {
        // like computing front vertex, we firstly compute the mapping vertex
        // on fold cylinder for point (x0, y0) which also is last vertex of
        // base shadow(backward direction)
        float x = x0 * cosA - y0 * sinA;
        float y = x0 * sinA + y0 * cosA;

        // compute mapping point on cylinder
        float rad = (x - tX)/ mR;
        x = (float)(tX + mR * Math.sin(rad));

        float cx1 = x * cosA + y * sinA + oX;
        float cy1 = y * cosA - x * sinA + oY;

        // now, we have start vertex(cx1, cy1), compute end vertex(cx2, cy2)
        // which is translated based on start vertex(cx1, cy1)
        float cx2 = cx1 + baseWcosA;
        float cy2 = cy1 - baseWsinA;

        // as we know, this function is only used to compute last vertex of
        // base shadow(backward) when the YFold points are outside page height,
        // that means the (cx1, cy1) and (cx2, cy2) we computed above normally
        // is outside page, so we need to compute their projection points on page
        // border as rendering vertex of base shadow
        float bx1 = cx1 + mKValue * (cy1 - dY);
        float bx2 = cx2 + mKValue * (cy2 - dY);

        // add start/end vertex into base shadow buffer, it will be linked with
        // forward vertexes to draw base shadow
        mFoldBaseShadow.addVertexes(false, bx1, dY, bx2, dY);
    }

    /**
     * Compute vertexes when page flip is slope
     */
    public void computeVertexesWhenSlope() {
        final PageModify page = this;
        final float oX = page.originP.x;
        final float oY = page.originP.y;
        final float dY = page.diagonalP.y;
        final float cOX = page.originP.texX;
        final float cOY = page.originP.texY;
        final float cDY = page.diagonalP.texY;
        final float height = page.height;
        final float d2oY = dY - oY;

        // compute radius and sin/cos of angle
        float sinA = (mFakeTouchP.y - oY) / mLenOfTouchOrigin;
        float cosA = (oX - mFakeTouchP.x) / mLenOfTouchOrigin;

        // need to translate before rotate, and then translate back
        int count = mMeshCount;
        float xFoldP1 = (mXFoldP1c.x - oX) * cosA;
        float edgeW = mFoldEdgesShadowWidth.width(mR);
        float baseW = mFoldBaseShadowWidth.width(mR);
        float baseWcosA = baseW * cosA;
        float baseWsinA = baseW * sinA;
        float edgeY = oY > 0 ? edgeW : -edgeW;
        float edgeX = oX > 0 ? edgeW : -edgeW;
        float stepSY = edgeY / count;
        float stepSX = edgeX / count;

        // reset vertexes buffer counter
        mFoldEdgesShadow.reset();
        mFoldBaseShadow.reset();
        mFoldFrontVertexes.reset();
        mFoldBackVertexes.reset();

        // add the first 3 float numbers is fold triangle
        mFoldBackVertexes.addVertex(mFakeTouchP.x, mFakeTouchP.y, 1, 0, cOX, cOY);

        // compute vertexes for fold back part
        float stepX = (mXFoldP0c.x - mXFoldPc.x) / count;
        float stepY = (mYFoldP0c.y - mYFoldPc.y) / count;
        float x = mXFoldP0c.x - oX;
        float y = mYFoldP0c.y - oY;
        float sx = edgeX;
        float sy = edgeY;

        // compute point of back of fold page
        // Case 1: y coordinate of point YFP0 -> YFP is < diagonalP.y
        //
        //   <---- Flip
        // +-------------+ diagonalP
        // |             |
        // |             + YFP
        // |            /|
        // |           / |
        // |          /  |
        // |         /   |
        // |        /    + YFP0
        // |       / p  /|
        // +------+--.-+-+ originP
        //      XFP   XFP0
        //
        // 1. XFP -> XFP0 -> originP -> YFP0 ->YFP is back of fold page
        // 2. XFP -> XFP0 -> YFP0 -> YFP is a half of cylinder when page is
        //    curled
        // 3. P point will be computed
        //
        // compute points within the page
        int i = 0;
        for (;i <= count && Math.abs(y) < height;
             ++i, x -= stepX, y -= stepY, sy -= stepSY, sx -= stepSX) {
            computeBackVertex(true, x, 0, x, sy, xFoldP1, sinA, cosA,
                    page.textureX(x + oX), cOY, oX, oY);
            computeBackVertex(false, 0, y, sx, y, xFoldP1, sinA, cosA, cOX,
                    page.textureY(y + oY), oX, oY);
        }

        // If y coordinate of point on YFP0 -> YFP is > diagonalP
        // There are two cases:
        //                      <---- Flip
        //     Case 2                               Case 3
        //          YFP                               YFP   YFP0
        // +---------+---+ diagonalP          +--------+-----+--+ diagonalP
        // |        /    |                    |       /     /   |
        // |       /     + YFP0               |      /     /    |
        // |      /     /|                    |     /     /     |
        // |     /     / |                    |    /     /      |
        // |    /     /  |                    |   /     /       |
        // |   / p   /   |                    |  / p   /        |
        // +--+--.--+----+ originalP          +-+--.--+---------+ originalP
        //   XFP   XFP0                        XFP   XFP0
        //
        // compute points outside the page
        if (i <= count) {
            if (Math.abs(y) != height) {
                // case 3: compute mapping point of diagonalP
                if (Math.abs(mYFoldP0c.y - oY) > height) {
                    float tx = oX + 2 * mKValue * (mYFoldPc.y - dY);
                    float ty = dY + mKValue * (tx - oX);
                    mFoldBackVertexes.addVertex(tx, ty, 1, 0, cOX, cDY);

                    float tsx = tx - sx;
                    float tsy = dY + mKValue * (tsx - oX);
                    mFoldEdgesShadow.addVertexes(false, tx, ty, tsx, tsy);
                }
                // case 2: compute mapping point of diagonalP
                else {
                    float x1 = mKValue * d2oY;
                    computeBackVertex(true, x1, 0, x1, sy, xFoldP1, sinA, cosA,
                            page.textureX(x1 + oX), cOY, oX, oY);
                    computeBackVertex(false, 0, d2oY, sx, d2oY, xFoldP1, sinA,
                            cosA, cOX, cDY, oX, oY);
                }
            }

            // compute the remaining points
            for (; i <= count;
                 ++i, x -= stepX, y -= stepY, sy -= stepSY, sx -= stepSX) {
                computeBackVertex(true, x, 0, x, sy, xFoldP1, sinA, cosA,
                        page.textureX(x + oX), cOY, oX, oY);

                // since the origin Y is beyond page, we need to compute its
                // projection point on page border and then compute mapping
                // point on curled cylinder
                float x1 = mKValue * (y + oY - dY);
                computeBackVertex(x1, d2oY, xFoldP1, sinA, cosA,
                        page.textureX(x1 + oX), cDY, oX, oY);
            }
        }

        mFoldBackVertexes.toFloatBuffer();

        // Like above computation, the below steps are computing vertexes of
        // front of fold page
        // Case 1: y coordinate of point YFP -> YFP1 is < diagonalP.y
        //
        //     <---- Flip
        // +----------------+ diagonalP
        // |                |
        // |                + YFP1
        // |               /|
        // |              / |
        // |             /  |
        // |            /   |
        // |           /    + YFP
        // |          /    /|
        // |         /    / |
        // |        /    /  + YFP0
        // |       /    /  /|
        // |      / p  /  / |
        // +-----+--.-+--+--+ originP
        //    XFP1  XFP  XFP0
        //
        // 1. XFP -> YFP -> YFP1 ->XFP1 is front of fold page and a half of
        //    cylinder when page is curled.
        // 2. YFP->XFP is joint line of front and back of fold page
        // 3. P point will be computed
        //
        // compute points within the page
        stepX = (mXFoldPc.x - mXFoldP1c.x) / count;
        stepY = (mYFoldPc.y - mYFoldP1c.y) / count;
        x = mXFoldPc.x - oX - stepX;
        y = mYFoldPc.y - oY - stepY;
        int j = 0;
        for (; j < count && Math.abs(y) < height; ++j, x -= stepX, y -= stepY) {
            computeFrontVertex(true, x, 0, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    page.textureX(x + oX), cOY, oX, oY, dY);
            computeFrontVertex(false, 0, y, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    cOX, page.textureY(y + oY), oX, oY, dY);
        }

        // compute points outside the page
        if (j < count) {
            // compute mapping point of diagonalP
            if (Math.abs(y) != height && j > 0) {
                float y1 = (dY - oY);
                float x1 = mKValue * y1;
                computeFrontVertex(true, x1, 0, xFoldP1, sinA, cosA,
                        baseWcosA, baseWsinA,
                        page.textureX(x1 + oX), cOY, oX, oY, dY);

                computeFrontVertex(0, y1, xFoldP1, sinA, cosA, cOX,
                        page.textureY(y1+oY), oX, oY) ;
            }

            // compute last pair of vertexes of base shadow
            computeBaseShadowLastVertex(0, y, xFoldP1, sinA, cosA,
                    baseWcosA, baseWsinA,
                    oX, oY, dY);

            // compute the remaining points
            for (; j < count; ++j, x -= stepX, y -= stepY) {
                computeFrontVertex(true, x, 0, xFoldP1, sinA, cosA,
                        baseWcosA, baseWsinA,
                        page.textureX(x + oX), cOY, oX, oY, dY);

                float x1 = mKValue * (y + oY - dY);
                computeFrontVertex(x1, d2oY, xFoldP1, sinA, cosA,
                        page.textureX(x1 + oX), cDY, oX, oY);
            }

        }

        // set uniform Z value for shadow vertexes
        mFoldEdgesShadow.vertexZ = mFoldFrontVertexes.getFloatAt(2);
        mFoldBaseShadow.vertexZ = -0.5f;

        // add two vertexes to connect with the unfold front page
        page.buildVertexesOfPageWhenSlope(mFoldFrontVertexes, mXFoldP1c, mYFoldP1c,
                mKValue);
        mFoldFrontVertexes.toFloatBuffer();

        // compute vertexes of fold edge shadow
        mFoldBaseShadow.toFloatBuffer();
        computeVertexesOfFoldTopEdgeShadow(mFakeTouchP.x, mFakeTouchP.y, sinA, cosA,
                -edgeX, edgeY);
        mFoldEdgesShadow.toFloatBuffer();
    }

    /**
     * Compute vertexes of fold top edge shadow
     * <p>Top edge shadow of fold page is a quarter circle</p>
     *
     * @param x0 X of touch point
     * @param y0 Y of touch point
     * @param sinA Sin value of page curling angle
     * @param cosA Cos value of page curling angle
     * @param sx Shadow width on X axis
     * @param sy Shadow width on Y axis
     */
    public void computeVertexesOfFoldTopEdgeShadow(float x0, float y0,
                                                    float sinA, float cosA,
                                                    float sx, float sy) {
        float sin2A = 2 * sinA * cosA;
        float cos2A = (float)(1 - 2 * Math.pow(sinA, 2));
        float r = 0;
        float dr = (float)(Math.PI / (FOLD_TOP_EDGE_SHADOW_VEX_COUNT - 2));
        int size = FOLD_TOP_EDGE_SHADOW_VEX_COUNT / 2;
        int j = mFoldEdgesShadow.mMaxBackward;

        //                 ^ Y                             __ |
        //      TouchP+    |                             /    |
        //             \   |                            |     |
        //              \  |                             \    |
        //               \ |              X <--------------+--+- OriginP
        //                \|                                 /|
        // X <----------+--+- OriginP                       / |
        //             /   |                               /  |
        //             |   |                              /   |
        //              \__+ Top edge              TouchP+    |
        //                 |                                  v Y
        // 1. compute quarter circle at origin point
        // 2. rotate quarter circle to touch point direction
        // 3. move quarter circle to touch point as top edge shadow
        for (int i = 0; i < size; ++i, r += dr, j += 8) {
            float x = (float)(sx * Math.cos(r));
            float y = (float)(sy * Math.sin(r));

            // rotate -2A and then translate to touchP
            mFoldEdgesShadow.setVertexes(j, x0, y0,
                    x * cos2A + y * sin2A + x0,
                    y * cos2A - x * sin2A + y0);
        }
    }

    /**
     * Compute mesh count for page flip
     */
    public void computeMeshCount() {
        float dx = Math.abs(mXFoldP0c.x - mXFoldP1c.x);
        float dy = Math.abs(mYFoldP0c.y - mYFoldP1c.y);
        int len = mIsVertical ? (int)dx : (int)Math.min(dx, dy);
        mMeshCount = 0;

        // make sure mesh count is greater than threshold, if less than it,
        // the page maybe is drawn unsmoothly
        for (int i = mPixelsOfMesh;
             i >= 1 && mMeshCount < MESH_COUNT_THRESHOLD;
             i >>= 1) {
            mMeshCount = len / i;
        }

        // keep count is even
        if (mMeshCount % 2 != 0) {
            mMeshCount++;
        }

        // half count for fold page
        mMeshCount >>= 1;
    }

    /**
     * Compute tan value of curling angle
     *
     * @param dy the diff value between touchP.y and originP.y
     * @return tan value of max curl angle
     */
    public float computeTanOfCurlAngle(float dy) {
        float ratio = dy / mViewRect.halfH;
        if (ratio <= 1 - MAX_PAGE_CURL_ANGLE_RATIO) {
            return MAX_PAGE_CURL_TAN_OF_ANGEL;
        }

        float degree = MAX_PAGE_CURL_ANGLE - PAGE_CURL_ANGEL_DIFF * ratio;
        if (degree < MIN_PAGE_CURL_ANGLE) {
            return MIN_PAGE_CURL_TAN_OF_ANGLE;
        }
        else {
            return (float)Math.tan(Math.PI * degree / 180);
        }
    }

    /**
     * Debug information
     */
    public void debugInfo() {
        //final GLPoint originP = originP;
        //final GLPoint diagonalP = diagonalP;

        Log.d(TAG, "************************************");
        Log.d(TAG, " Mesh Count:    " + mMeshCount);
        //Log.d(TAG, " Mesh Pixels:   " + mPixelsOfMesh);
        Log.d(TAG, " Origin:        " + originP.x + ", " + originP.y);
        Log.d(TAG, " Diagonal:      " + diagonalP.x + ", " + diagonalP.y);
        //Log.d(TAG, " OriginTouchP:  " + mStartTouchP.x + ", " + mStartTouchP.y);
        //Log.d(TAG, " TouchP:        " + mTouchP.x + ", " + mTouchP.y);
        Log.d(TAG, " MiddleP:       " + mMiddleP.x + ", " + mMiddleP.y);
        Log.d(TAG, " XFoldP:        " + mXFoldPc.x + ", " + mXFoldPc.y);
        Log.d(TAG, " XFoldP0:       " + mXFoldP0c.x + ", " + mXFoldP0c.y);
        Log.d(TAG, " XFoldP1:       " + mXFoldP1c.x + ", " + mXFoldP1c.y);
        Log.d(TAG, " YFoldP:        " + mYFoldPc.x + ", " + mYFoldPc.y);
        Log.d(TAG, " YFoldP0:       " + mYFoldP0c.x + ", " + mYFoldP0c.y);
        Log.d(TAG, " YFoldP1:       " + mYFoldP1c.x + ", " + mYFoldP1c.y);
        Log.d(TAG, " LengthT->O:    " + mLenOfTouchOrigin);
    }

}
