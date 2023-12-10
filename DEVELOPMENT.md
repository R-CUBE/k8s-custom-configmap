# Developers guide

The operator is a Spring Boot project. To build the project, you need the following prerequisites:
- A local installation of JDK17
- Gradle `./gradlew clean build` to execute the tests and build the operator.

To test and deploy the operator in your local environment, you need the following setup:
- [Docker](https://www.docker.com/) to build the operator image with your local changes.
- [minikube](https://minikube.sigs.k8s.io/docs/start/) to deploy the operator and test your changes.

## How I test my changes

Once I have made some changes to the code, I follow the below workflow:

- Run unit and integration tests.
- Build operator
  ```
    ~ ./gradlew clean build
  ```
- Build the operator docker image with the new changes.
  ```
    ~ minikube image build -t configs-operator:test_5 .
  ```
  Note that I used minikube client to build the docker image. Quote from [minikube](https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env)
  > When using a container or VM driver (all drivers except none), you can reuse the Docker daemon inside minikube cluster. This means you don’t have to build on your host machine and push the image into a docker registry. You can just build inside the same docker daemon as minikube which speeds up local experiments.
- Update [./install/cluster/Config-Operator.yaml](https://github.com/rameshmalla/k8s-custom-configmap/blob/main/install/cluster/Config-Operator.yaml#L31) with your new test image `configs-operator:test_5`.
- Apply the CRD and operator manifest in the `./install` folder.
  ```shell
        ~ kubectl apply -f ./install/cluster/configmapcustomresources.rcube.com-v1.yml       
          customresourcedefinition.apiextensions.k8s.io/configmapcustomresources.rcube.com created
  
        ~ kubectl apply -f ./install/cluster/Config-Operator.yaml
          namespace/configs-operator created
          serviceaccount/configs-operator created
          deployment.apps/configs-operator created
          clusterrole.rbac.authorization.k8s.io/configs-operator-role created
          clusterrolebinding.rbac.authorization.k8s.io/configs-operator-admin created
   ```
- Apply application [./install/testing/app-deployment.yaml](https://github.com/rameshmalla/k8s-custom-configmap/blob/main/install/testing/app-deployment.yaml) manifest.
  ```shell
      ~ kubectl apply -f ./install/testing/app-deployment.yaml                             
        namespace/local created
        configmapcustomresource.rcube.com/application-toggles-config created
        deployment.apps/nginx-deployment created
  ```

- Test my changes against the deployed application and the operator.