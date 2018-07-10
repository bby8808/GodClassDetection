from keras.models import Sequential, load_model, Model, model_from_json
from keras.layers import Conv1D, MaxPooling1D, Embedding,LSTM,GaussianNoise,Masking
from keras.layers import Dense, Flatten, Merge, Dropout, Reshape, Input,BatchNormalization,Activation
from keras import regularizers
from sklearn import metrics
import matplotlib.pyplot as plt
import keras
import os
import shutil
import preprocess
import time
#os.environ["CUDA_VISIBLE_DEVICES"] = "0"

localtime = time.asctime(time.localtime(time.time()))
print ("begin time",localtime)

EMBEDDING_DIM=200
MAX_SEQUENCE_LENGTH = 50
MAX_JACCARD_LENGTH = 30
INC_BATCH_SIZE = 80000

date_id = '0512'

BASE_DIR = ''
W2V_MODEL_DIR = '../../embedding_model/new_model20180517/new_model20180517.bin'
TRAIN_SET_DIR = '../../trainset/trainset_'+date_id
#FULL_MN_DIR = '../trainset/trainset_'+date_id+'/full_mn/mn_full.txt'
FULL_MN_DIR = TRAIN_SET_DIR
TRAINED_MODEL_WEIGHT_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_weights'
TRAINED_MODEL_JSON_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_json'
TRAINED_MODEL_LOG_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_log'
TRAINED_MODEL_FIG_DIR = '../../trained_model/'+'trained_model_'+date_id+'/trained_model_fig'

tokenizer = preprocess.get_tokenizer(FULL_MN_DIR)
all_word_index = tokenizer.word_index
embedding_matrix = preprocess.get_embedding_matrix(all_word_index, W2V_MODEL_DIR, dim=EMBEDDING_DIM)

acc_list = []
loss_list = []

test_path = TRAIN_SET_DIR+'/test'

trained_weight_path = TRAINED_MODEL_WEIGHT_DIR+'.h5'
trained_json_path = TRAINED_MODEL_JSON_DIR+'.json'
trained_model_log = TRAINED_MODEL_LOG_DIR+'.txt'
trained_moedl_fig = TRAINED_MODEL_FIG_DIR+'.png'
if os.path.exists(trained_weight_path):
        for i in range(10):
                trained_weight_path = TRAINED_MODEL_WEIGHT_DIR+'_'+str(i)+'.h5'
                trained_json_path = TRAINED_MODEL_JSON_DIR+'_'+str(i)+'.json'
                trained_model_log = TRAINED_MODEL_LOG_DIR+'_'+str(i)+'.txt'
                trained_moedl_fig = TRAINED_MODEL_FIG_DIR+'_'+str(i)+'.png'
                if os.path.exists(trained_weight_path)==False:
                        break

project = 'manual-ant-1.8.2'
x_train, y_train= preprocess.get_xy_train(TRAIN_SET_DIR+'/train',tokenizer=tokenizer, mn_maxlen=MAX_SEQUENCE_LENGTH,embedding_matrix=embedding_matrix)
#x_test, y_test = preprocess.get_xy_test(test_path,project,tokenizer=tokenizer, maxlen=MAX_SEQUENCE_LENGTH,embedding_matrix=embedding_matrix)

print('Training model.')
# load pre-trained word embeddings into an Embedding layer
# note that we set trainable = False so as to keep the embeddings fixed
method_a = Input(shape=(MAX_SEQUENCE_LENGTH,EMBEDDING_DIM),name='method_a')
metric_a = Input(shape=(12,),name='metric_a')

#embedding_layer = Embedding(len(all_word_index) + 1,EMBEDDING_DIM,input_length=MAX_SEQUENCE_LENGTH,weights=[embedding_matrix],trainable=False)
masking_layer = Masking(mask_value=0,input_shape=(MAX_SEQUENCE_LENGTH,EMBEDDING_DIM))
lstm_share = LSTM(output_dim=2,activation='sigmoid',init='uniform')
#dropout_share = GaussianNoise(0.2)
#dense_share = Dense(8,activation='tanh',init='uniform')
#dense_share3 = Dense(64,activation='tanh',init='uniform')
#bn_share = BatchNormalization(epsilon=0.001, mode=0, axis=-1, momentum=0.9, weights=None, beta_init='zero', gamma_init='one')

embedding_a = masking_layer(method_a)
lstm_a = lstm_share(embedding_a)
#dropout_a = dropout_share(lstm_a)
#bn_a = bn_share(lstm_a)
#encoded_a = dense_share(dropout_a)
#decoded_a = dense_share3(encoded_a)

#merged_vector = keras.layers.dot([encoded_a,encoded_b],normalize=True,axes=-1)
#dense_vector = Dense(16,activation='tanh')(merged_vector)

dense_share2 = Dense(12,activation='tanh',init='uniform')
#bn_share2 = BatchNormalization(epsilon=0.001, mode=0, axis=-1, momentum=0.9, weights=None, beta_init='zero', gamma_init='one')
#dropout_share2 = GaussianNoise(0.8)

mtrdense_a= dense_share2(metric_a)
#mtr_dropout_a = dropout_share2(mtrdense_a)
#jac_merged = keras.layers.dot([jac_dropout_a,jac_dropout_b],normalize=True,axes=-1)

m_j_merged_a = keras.layers.concatenate([lstm_a,mtrdense_a],axis=-1)
dense1_a = Dense(4,activation='tanh',init='zero')(m_j_merged_a)
#dense2_a = Dense(2,activation='tanh',init='zero')(dense1_a)
total_dropout = Dropout(0.6)(dense1_a)
total_output = Dense(1,activation='sigmoid',name='output')(total_dropout)
model_final = Model(inputs=[method_a,metric_a],output=total_output)
#print model_final.summary()

#input()
sgd = keras.optimizers.SGD(lr=0.1, momentum=0.9, decay=0.0, nesterov=False)
model_final.compile(loss='binary_crossentropy',optimizer='adam',metrics=['binary_accuracy'])
hist = model_final.fit(x_train, y_train,nb_epoch=10,batch_size=5,verbose=1)
#hist = model_final.fit(x_train, y_train,nb_epoch=10,batch_size=1,validation_data=[x_test,y_test])
#hist = model_final.fit(x_train, y_train,nb_epoch=10,batch_size=5,validation_split=0.1)
score = model_final.evaluate(x_train, y_train, verbose=0)
print('train mse:', score[0])
print('train accuracy:', score[1])
"""score = model_final.evaluate(x_test, y_test, verbose=0)
#print model_final.metrics_names
print('Test mse:', score[0])
print('Test accuracy:', score[1])
loss_list.append(score[0])
acc_list.append(score[1])

predict_posibility = model_final.predict(x_test)
#print predict_posibility
predict_label = []
#godclass_index = []
for i,item in enumerate(predict_posibility):
        if item<=0.68:
                predict_label.append(0)
        else:
                predict_label.append(1)
                #godclass_index.append((i*14)+946)
precision = metrics.precision_score(y_test,predict_label)
recall = metrics.recall_score(y_test,predict_label)
f1_score = metrics.f1_score(y_test,predict_label)
print ('test precision:',precision)
print ('test recall', recall)
print ('test f1 score:',f1_score)
print predict_label
#print y_test
#print godclass_index
f1_scores.append(f1_score)"""

json_string = model_final.to_json()
splited_path = os.path.split(trained_json_path)

if not os.path.exists(splited_path[0]):
        os.makedirs(splited_path[0])
with open(trained_json_path,'w') as f:
        f.write(json_string)
model_final.save_weights(trained_weight_path)

"""if not os.path.exists(trained_model_log):
                splited_path = os.path.split(trained_model_log)
                os.makedirs(splited_path[0])
        with open(trained_model_log,'w') as f:
                f.write(str(hist.history))
                f.write('test loss '+str(score[0])+' test acc '+str(score[1])+' test f1 score:'+str(f1_score))

        fig = plt.figure()
        plt.plot(hist.history['binary_accuracy'])
        # plt.plot(hist.history['val_acc'])
        plt.title('model accuracy')
        plt.ylabel('accuracy')
        plt.xlabel('epoch')
        # plt.legend(['train','test'],loc='upper left')
        plt.plot(hist.history['binary_crossentropy'])
        # plt.plot(hist.history['val_loss'])
        plt.title('model loss')
        plt.ylabel('loss')
        plt.xlabel('epoch')
        #plt.legend(['train','test'],loc='lower left')
        if not os.path.exists(trained_moedl_fig):
                splited_path = os.path.split(trained_moedl_fig)
                os.makedirs(splited_path[0])
        fig.savefig(trained_moedl_fig)"""
localtime = time.asctime(time.localtime(time.time()))
print ("begin time",localtime)

avg_accuracy = sum(acc_list)/len(acc_list)
avg_loss = sum(loss_list)/len(loss_list)

print("average accuracy: ",avg_accuracy)
print("average loss: ",avg_loss)

