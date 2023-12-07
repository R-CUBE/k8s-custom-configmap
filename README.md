# k8s-custom-configmap

`k8s-custom-configmap` is a custom k8s config-map resource which includes JSON data validation and create 
versioned config-maps as requested by the application.

## Components

### Custom config-map resource

The custom config-map resource is an extension of the native k8s config-map resource. The custom resource has an additional required field `schema` 
where the users can define the schema for the respective data in the config map.

**Example**
```yaml
apiVersion: "rcube.com/v1"
kind: ConfigMapCustomResource
metadata:
  namespace: your-namespace
  name: application-toggles-config
spec:
  config:
    data:
        application-toggles-config.json: |
        {
        "startNewFlow": true
        }
    schema:
        application-toggles-config.json: |
        {
            "type": "object",
            "properties": {
                "startNewFlow": {
                    "type": "boolean"
                }
            },
            "required": [
              "startNewFlow"
            ]
          }
```

### config-operator

The config-operator watches config-map CRD resources and k8s deployment resources. The operator is responsible for the below operations.
- Validate custom config-map resources against the schema
- Create config-maps from the custom resource.
- Restricts direct changes to the native config-map resource.
- Create versioned custom config-map resources for the applications.

## Features

### Validate config-map JSON data against JSON schema


### Restricts direct changes to the native config-map resource
The config-operator monitors the dependent resources (k8s native config-map) of the custom config-map and resets any manual changes made to the dependent resources. This is done to ensure that config-maps can only be updated through changes to the custom config-map resource, thereby ensuring proper validations.


### config-map versioning


## Installation

Apply the [CRD]() in your k8s cluster
```shell
kubectl apply -f ./install/cluster/configmapcustomresources.rcube.com-v1.yml
```

Deploy [config-operator]() in your k8s cluster.
```shell
kubectl apply -f ./install/cluster/Config-Operator.yaml
```
**Note**: The operator would create a namespace `configs-operator` in your cluster.

## Usage

Once the CRD and the operator is installed you can start defining your `ConfigMapCustomResource` as below

```yaml
apiVersion: "rcube.org/v1"
kind: ConfigMapCustomResource
metadata:
  namespace: your-namespace
  name: application-toggles-config
spec:
  config:
    data:
        application-toggles-config.json: |
        {
        "startNewFlow": true
        }
    schema:
        application-toggles-config.json: |
        {
            "type": "object",
            "properties": {
                "startNewFlow": {
                    "type": "boolean"
                }
            },
            "required": [
              "startNewFlow"
            ]
          }
```

This would create a config-map with the name `application-toggles-config` as below.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: "application-toggles-config"
  namespace: "{{{NAMESPACE}}}"
  labels:
    application: "{{{APPLICATION}}}"
data:
  application-toggles-config.json: |
    {
      "startNewFlow": true
    }
```