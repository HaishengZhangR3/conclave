

The scenario: 
	Each hospital has some patient data (diabetes patient), which is very valuable and will never been shared to anyone else. 
	Meanwhile, they want to train a machine learning algorithm to detect new patient based on as many as possible patient data, so they'd like to use the patient data from other hospital. This is a contradict requirement.


Four components: 
	Two hospitals each of which is a Corda node and a Conclave client, it send a set of patient data to enclave for training, and send one patient data for predicting. The CorDapp provides two API: hospital provides data to do machine learning training, and, hospital sends new patient data to do predict (negative or positive).
	Conclave host which hosts the enclave component, 
	an enclave where machine learning algorithm is implemented inside to train the diabetes data and predict patient data.


Run it:
	./gradlew host:run

	run two hospitals Corda nodes