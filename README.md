# Stakeholders Recommender

_This service was created as a result of the OpenReq project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732463._

## Introduction

The **stakeholders recommender** component is a service addressed to evaluate and execute assignation proposals of requirements inside a project to a specific stakeholder working in a project.

## Technical description

Next sections provide a general overview of the technical details of the stakeholders recommender service.

### Functionalities

#### Stakeholder recommendation

 - Main methods
    - batch_process: Adds the historic information of a set of stakeholders belonging to a specific organization. 
    - recommend: Returns, for a requirement, an ordered list of the best possible stakeholders to fulfill it.
    - reject_recommendation: Blocks a stakeholder from appearing again in recommendations made by the specific user for a specific requirement.
    - undoRejection: Undoes a reject_recommendation.

 - Auxiliary methods
    - getPersonSkills: Returns, for a stakeholder, its set of skills.
    - setEffort: Allows setting the conversion of effort points to hours.
    - computeEffort: Computes, given a historic information, the conversion of effort points to hours.

### Used technologies

* Swagger (&rarr; [https://swagger.io/](https://swagger.io/) )
* Maven (&rarr; [https://maven.apache.org/](https://maven.apache.org/) )
* GloVe: GloVe model (&rarr; [https://nlp.stanford.edu/projects/glove/](https://nlp.stanford.edu/projects/glove/) )
* riLogging: OpenReq microservice (&rarr; [https://github.com/OpenReqEU/ri-logging](https://github.com/OpenReqEU/ri-logging) )
* keywords_preprocessing: OpenReq microservice (&rarr; [https://github.com/OpenReqEU/keywords-extraction](https://github.com/OpenReqEU/keywords-extraction) )

### How to install

    1. Download the gloVe model from the latest release, and uncompress it on the root folder of the project

    2. Download and install 8 JDK and last Maven version. 

    3. To generate the jar file use

	mvn clean install package


### How to use it

You can take a look at the Swagger documentation [here](https://api.openreq.eu/#/services/stakeholders-recommender), which includes specific, technical details of the REST API to communicate to the service.

If that url is down, execute the jar and look at the Swagger documentation in "http://localhost:9410/swagger-ui.html" .

### Notes for developers

### Sources


## How to contribute

See OpenReq project contribution [guidelines](https://github.com/OpenReqEU/OpenReq/blob/master/CONTRIBUTING.md)

## License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/)
