The scenario: 
	Each hospital has some patient data (diabetes patient), which is very valuable and will never been shared to anyone else. 
	Meanwhile, they want to train a machine learning algorithm to detect new patient based on as many as possible patient data, so they'd like to use the patient data from other hospital. This is a contradict requirement.


Three parts in this standalone version:
	client: stand alone client, send a set of patient data to enclave for training, and send one patient data for predicting.
	host: a web service (not RESTful API with spring boot yet).
	enclave: machine learning algorithm is implemented inside to train the diabetes data and predict patient data.


Run it:
	./gradlew host:run

	./gradlew client:run