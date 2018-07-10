from __future__ import division
from keras.models import model_from_json
import numpy as np
import preprocess
import scipy.cluster.hierarchy as hierarchy
import scipy.spatial.distance as distance
import matplotlib.pylab as plt
from sklearn import metrics
import os
import shutil
import time

localtime = time.asctime(time.localtime(time.time()))
print ("begin time",localtime)

date_id = '0512'
k = 0
TRAINED_MODEL_JSON_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_json.json'
TRAINED_MODEL_WEIGHT_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_weights.h5'
TRAIN_SET_DIR = '../../trainset/trainset_'+date_id
FULL_MN_DIR = '../../trainset/trainset_'+date_id
W2V_MODEL_DIR = '../../embedding_model/new_model20180517/new_model20180517.bin'

MAX_SEQUENCE_LENGTH =50
MAX_JACCARD_LENGTH =30
EMBEDDING_DIM=200

f1_scores = []
tokenizer = preprocess.get_tokenizer(FULL_MN_DIR)
all_word_index = tokenizer.word_index
embedding_matrix = preprocess.get_embedding_matrix(all_word_index, W2V_MODEL_DIR, dim=EMBEDDING_DIM)
test_path = TRAIN_SET_DIR+'/test/'
initial_line = {'manual-FreeMind_integration':374, 'manual-ant-1.8.2':946,'manual-derby-10.9.1.0':1986,'manual-hadoop-1.1.2':1770,'manual-jhotdraw-7.5.1':436,'manual-struts-2.2.1':926}
f=open(TRAINED_MODEL_JSON_DIR)
json_string = f.read()
f.close()
model = model_from_json(json_string)
model.load_weights(TRAINED_MODEL_WEIGHT_DIR)
model.compile(loss='binary_crossentropy',optimizer='adam',metrics=['accuracy'])

for project in sorted(os.listdir(test_path)):
	print project
	x_val, y_val = preprocess.get_xy_test(test_path,project,tokenizer=tokenizer, maxlen=MAX_SEQUENCE_LENGTH,embedding_matrix=embedding_matrix)

	score = model.evaluate(x_val, y_val, verbose=0)
	#print model.metrics_names
	print('Test score:', score[0])
	print('Test accuracy:', score[1])
	#input()
	predict_posibility = model.predict(x_val)

	predict_label = []
	godclass_indices = []
	for i,item in enumerate(predict_posibility):
		if item <= 0.67674383521080011:
			predict_label.append(0)
		else:
			godclass_indices.append(i)
			predict_label.append(1)

	labeld_godclasses = []
	for i,item in enumerate(y_val):
		if item == 1:
			labeld_godclasses.append(i)
	precision = metrics.precision_score(y_val,predict_label)
	recall = metrics.recall_score(y_val,predict_label)
	f1_score = metrics.f1_score(y_val,predict_label)

	"""godclasses_lines = []
	for index in godclass_indices:
		godclasses_lines.append(index*14+initial_line[project])"""

	print ('test precision:',precision)
	print ('test recall', recall)
	print ('test f1 score:',f1_score)
	#print predict_label
	#print y_test
	"""print ('detected god class: ',godclass_indices)
	print ('labeld god class: ',labeld_godclasses)
	print ('god classes lines: ',godclasses_lines)"""
	f1_scores.append(f1_score)
	#input()
avg_f1 = sum(f1_scores)/len(f1_scores)
print("average f1: ",avg_f1)
localtime = time.asctime(time.localtime(time.time()))
print ("end time",localtime)