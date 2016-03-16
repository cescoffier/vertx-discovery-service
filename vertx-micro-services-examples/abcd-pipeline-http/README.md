# Micro-Service example - ABCD - Pipeline using HTTP
 
 
In this example are involved 4 services exposed as REST: A, B and C.
A also publishes a HTML page calling A, that call B, that call C.
 
## OpenShift instruction
 
The projects are built using the vert.x source to image support.
 
```
oc login
# enter admin/admin
oc new-project vertx-msa-abcd-pipeline-http
oc create -f https://raw.githubusercontent.com/cescoffier/vertx-s2i/master/vertx-s2i-all.json -n vertx-msa-abcd-pipeline-http
# Wait until the vertx-s2i image has been built, you can check with `oc status`
# ....
oc create -f C/openshift.json
oc create -f B/openshift.json
oc create -f A/openshift.json

# Check whether or not the builds start automatically, if not run them using:
#
# oc start-build bc/c
# oc start-build bc/d
# oc start-build bc/b
# oc start-build bc/a


# login to https://10.2.2.2:8443, browse the vertx-msa-abcd-pipeline-http project
# Pods should be built and created
# Go to browse - builds to has the current builds
```


