# Introduction

This plugin provides an extension for the [kubernetes-credentials-provider-plugin](https://github.com/jenkinsci/kubernetes-credentials-provider-plugin)
plugin, and the [openshift-client-plugin](https://github.com/jenkinsci/openshift-client-plugin) that extend the kubernetes credentials provider to create the special credential type required by the openshift-client-plugin when interacting with an openshift cluster.

## Usage

This plugin consumes extends the kubernetes-credentials-provider-plugin to consume kubernetes secrets with a `"jenkins.io/credentials-type"` of `"openshiftToken"`. These secrets need to have a data property `"text"` that contains a base64 encoded `token` for openshift cluster.

### Example

```
apiVersion: v1
data:
  text: c3VwZXJkdXBlcnNlY3JldA==
kind: Secret
metadata:
  annotations:
    jenkins.io/credentials-description: The Openshift token to access the cluster
  labels:
    jenkins.io/credentials-type: openshiftToken
  name: my-openshift-secret
  namespace: jenkins-demo
type: Opaque
```
