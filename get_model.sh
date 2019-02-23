curl -L http://download.tensorflow.org/models/object_detection/ssd_inception_v2_coco_2017_11_17.tar.gz | tar -xz
mv ssd_inception_v2_coco_2017_11_17/saved_model ./src/main/resources/tf_inception
rm -r ssd_inception_v2_coco_2017_11_17