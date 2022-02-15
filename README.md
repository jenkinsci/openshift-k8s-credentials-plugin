# openshift-k8s-credentials

[![Build Status](https://ci.jenkins.io/job/Plugins/job/openshift-k8s-credentials-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/openshift-k8s-credentials-plugin/job/main/)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/openshift-k8s-credentials-plugin.svg?label=changelog)](https://github.com/jenkinsci/openshift-k8s-credentials-plugin/releases/latest)


## Introduction

This plugin provides an extension for the [kubernetes-credentials-provider-plugin](https://github.com/jenkinsci/kubernetes-credentials-provider-plugin)
plugin, and the [openshift-client-plugin](https://github.com/jenkinsci/openshift-client-plugin) that extend the kubernetes credentials provider to create the special credential type required by the openshift-client-plugin when interacting with an openshift cluster.

## Getting started

The plugin consume Kubernetes credentials of type 'openshiftToken'.

You will find an example bellow

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
  name: my-openshift-token
  namespace: jenkins-demo
type: Opaque
```

This will create the secret on the Kubernetes credentials store.

![](openshift_token.png)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)
