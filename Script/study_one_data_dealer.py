import os
os.environ['PYTHON_EGG_CACHE'] = '/temp'

import numpy as np
import csv
import argparse


#clean each data
def clean_raw_data():
	#change this path based on machine
	data_dir = "/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data"
	raw_cleaned_data = []
	cleaned_data = []
	#state 0 - nothing, 1 - start, 2 - during, 3 - end

	for subdir, dirs, files in os.walk(data_dir):
		for file in files:
			if "data_" in file:
				print(os.path.join(subdir, file))
				single_file_data = open(os.path.join(subdir, file))
				user_index = subdir
				user_index = user_index[-1:]
				print(user_index)
				csv_f = csv.reader(single_file_data)


				# old_row = []

				# for row in csv_f:
				# 	if len(old_row) > 0:
				# 		if row[7] == '-1' and old_row[7] == '-1':
				# 			#a repeated start, delete the previous one
				# 			print("repeated")
				# 			cleaned_data = cleaned_data[:-1]

				# 	old_row = list(row)
				# 	row.insert(0, user_index)
				# 	cleaned_data.append(row)

				for row in csv_f:
					if row[10] == '3':
						if row[12] == '1':
							row.insert(0, user_index)
							raw_cleaned_data.append(row)

				old_row = []

				for row in raw_cleaned_data:
					if len(old_row) > 0:
						if row[1] == old_row[1]:
							#repeated
							cleaned_data = cleaned_data[:-1]

					old_row = list(row)
					cleaned_data.append(row)


	with open('/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data/cleaned_data.csv', 'w') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow(['user', 'trial', 'attempt', 'corner', 'anglenum', 'distancenum', 'closeness', 'angletarget', 'distancetarget', 'angleactual', 'distanceactual', 'state', 'timestamp', 'iscorrect', 'visitedcells', 'numovershot', 'duration'])
		for data in cleaned_data:
			writer.writerow(data)
	
	# with open("/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data/cleaned_data.txt", "w") as text_file:
	# 	title = ['user', 'trial', 'corner', 'anglenum', 'distancenum', 'closeness', 'angletarget', 'distancetarget', 'angleactual', 'distanceactual','timestamp']
	# 	text_file.write("{}".format(title))
	# 	for data in cleaned_data:
	# 		text_file.write("{}".format(data))


#put all data together
def create_all_raw():
	data_dir = "study_data"
	all_data = []

	for subdir, dirs, files in os.walk(data_dir):
		for file in files:
			if 'data_' in file:
				print(os.path.join(subdir, file))
				single_file_data = open(os.path.join(subdir, file))
				user_index = subdir
				user_index = user_index[20:]
				csv_f = csv.reader(single_file_data)
				#for row in csv_f:



if __name__ == "__main__":

	parser = argparse.ArgumentParser(description='data_dealer --step string')
	parser.add_argument('--step', action='store', dest='step', default='0' ,help='step to execute')

	args = parser.parse_args()

	if args.step == "0":
		print("expecting > 0")
	elif args.step == '1':
		clean_raw_data()
