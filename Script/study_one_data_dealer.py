import os
os.environ['PYTHON_EGG_CACHE'] = '/temp'

import numpy as np
import csv
import argparse



def fix_raw_data():
	data_dir = "/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data/user_12"
	#data belongs to the same trial
	trial_data = []
	trial = -1
	fixed_data = []
	#state 0 - nothing, 1 - start, 2 - during, 3 - end

	for subdir, dirs, files in os.walk(data_dir):
		for file in files:
			if "data_" in file:
				print(os.path.join(subdir, file))
				single_file_data = open(os.path.join(subdir, file))
				csv_f = csv.reader(single_file_data)

				for row in csv_f:
					if row[0] == trial and (int(row[8]) + int(row[9]) != -2 ):
						trial_data.append(row)
					else:
						#deal the trial data
						if len(trial_data) > 2:
							#delete the second one and save to fixed data
							del trial_data[1]

						overshot = 0
						for trial_row in trial_data:

							if int(trial_row[9]) - int(trial_row[7]) > overshot:
								overshot = int(trial_row[9]) - int(trial_row[7])

							if trial_row[10] == '3':
								#print("test trial end")
								trial_row[14] = overshot

							fixed_data.append(trial_row)

						#empty trial data
						del trial_data[:]
						trial_data.append(row)
						trial = row[0]

				#the last trial
				#deal the trial data
				if len(trial_data) > 2:
					#delete the second one and save to fixed data
					del trial_data[1]

				overshot = 0
				for trial_row in trial_data:

					if int(trial_row[9]) - int(trial_row[7]) > overshot:
						overshot = int(trial_row[9]) - int(trial_row[7])

					if trial_row[10] == '3':
						#print("test trial end")
						trial_row[14] = overshot

					fixed_data.append(trial_row)

				#empty trial data
				del trial_data[:]
				trial_data.append(row)
				trial = row[0]


	with open('/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data/user_12/fixed_data.csv', 'w') as csvfile:
		writer = csv.writer(csvfile)
		for data in fixed_data:
			writer.writerow(data)


#clean each data
def clean_raw_data():
	#change this path based on machine
	data_dir = "/Users/hanteng/Dropbox/Ring-TextEntry-Flip/flippage_study/study_one_data"
	raw_cleaned_data = []
	cleaned_data = []
	#state 0 - nothing, 1 - start, 2 - during, 3 - end

	for subdir, dirs, files in os.walk(data_dir):
		for file in files:
			if "fixed_data" in file:
				print(os.path.join(subdir, file))
				single_file_data = open(os.path.join(subdir, file))
				user_index = subdir

				if user_index[-2] == '_':
					user_index = user_index[-1:]
				elif user_index[-3] == '_':
					user_index = user_index[-2:]
				print(user_index)
				csv_f = csv.reader(single_file_data)

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


#study two
def clean_raw_data_2():
	#change this path based on machine
	data_dir = "/Users/hanteng/Dropbox/Ring-TextEntry-Flip/study_two/study_two_data"
	raw_cleaned_data = []
	cleaned_data = []
	#state 0 - nothing, 1 - start, 2 - during, 3 - end

	for subdir, dirs, files in os.walk(data_dir):
		for file in files:
			if "data_" in file:
				print(os.path.join(subdir, file))
				
				single_file_data = open(os.path.join(subdir, file))
				user_index = subdir

				if user_index[-2] == '_':
					user_index = user_index[-1:]
				elif user_index[-3] == '_':
					user_index = user_index[-2:]
				print(user_index)
				csv_f = csv.reader(single_file_data)

				for row in csv_f:
					if row[3] == '5':
						if row[13] == '1':
							row.insert(0, user_index)
							raw_cleaned_data.append(row)

	old_row = []
	for row in raw_cleaned_data:
		if len(old_row) > 0:
			if row[2] == old_row[2]:
				#repeated
				cleaned_data = cleaned_data[:-1]

		old_row = list(row)
		cleaned_data.append(row)


	with open('/Users/hanteng/Dropbox/Ring-TextEntry-Flip/study_two/study_two_data/cleaned_data.csv', 'w') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow(['user', 'technique', 'trial', 'attempt', 'state', 'timestamp', 'corner', 'task', 'tasktype', 'close', 'angletarget', 'distancetarget', 'angleactual', 'distanceactual', 'iscorrect', 'iswrongtype', 'numovershot', 'duration', 'preparetime', 'menutime', 'tasktime', 'touchcount'])
		for data in cleaned_data:
			writer.writerow(data)


if __name__ == "__main__":

	parser = argparse.ArgumentParser(description='data_dealer --step string')
	parser.add_argument('--step', action='store', dest='step', default='0' ,help='step to execute')

	args = parser.parse_args()

	if args.step == "0":
		print("expecting > 0")
	# elif args.step == '1':
	# 	fix_raw_data()
	# elif args.step == '2':
	# 	clean_raw_data()
	elif args.step == '3':
		clean_raw_data_2()
