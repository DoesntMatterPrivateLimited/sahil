# Jenkins Shared Library — Task 3

This project implements a reusable **Jenkins Shared Library** containing common CI/CD pipeline functions, and a sample pipeline that consumes them.

---

## 📁 Repository Structure

**Shared Library repo:** `jenkins-shared-library`

```
jenkins-shared-library/
└── vars/
    ├── gitCheckout.groovy
    ├── buildApp.groovy
    ├── dockerBuild.groovy
    ├── dockerPush.groovy
    ├── deployK8s.groovy
    ├── helmDeploy.groovy
    ├── slackNotify.groovy
    ├── rollback.groovy
    └── cleanup.groovy
```

Each file in `vars/` is a **global variable script**. Jenkins auto-loads these and exposes each one as a callable function (e.g. `buildApp()`) inside any pipeline that imports the library.

**App repo:** `Multibranch-Pipeline` — contains the `Jenkinsfile` that consumes the shared library.

---

## 🔧 Functions Implemented

| Function | Purpose |
|---|---|
| `gitCheckout(branch, repoUrl)` | Checks out source code from a Git repository |
| `buildApp()` | Builds the application |
| `dockerBuild(imageName, tag)` | Builds a Docker image |
| `dockerPush(imageName, tag)` | Pushes a Docker image to a registry |
| `deployK8s(manifestPath)` | Applies a Kubernetes manifest |
| `helmDeploy(releaseName, chartPath, namespace)` | Deploys/upgrades a Helm release |
| `slackNotify(message, channel)` | Sends a Slack notification |
| `rollback(releaseName, namespace)` | Rolls back a Helm release |
| `cleanup()` | Cleans up workspace/Docker resources |

> Currently, `buildApp`, `dockerBuild`, `dockerPush`, `deployK8s`, `helmDeploy`, `slackNotify`, `rollback`, and `cleanup` use **placeholder logic (`echo` statements)** since Maven, Docker, kubectl, Helm, and Slack integrations are not yet configured on the Jenkins agent. `gitCheckout` is fully functional.

---

## ⚙️ Jenkins Configuration

The library is registered under:

**Manage Jenkins → System → Global Trusted Pipeline Libraries**

| Setting | Value |
|---|---|
| Name | `jenkins-shared-library` |
| Retrieval method | Modern SCM → Git |
| Project Repository | `https://github.com/sahilhinge89/jenkins-shared-library.git` |
| Credentials | none (public repo) |

Since no default version was set, the pipeline explicitly pins the branch using:

```groovy
@Library('jenkins-shared-library@main') _
```

---

## 🚀 Pipeline (Jenkinsfile)

Located in the `Multibranch-Pipeline` repo:

```groovy
@Library('jenkins-shared-library@main') _

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                gitCheckout('main', 'https://github.com/sahilhinge89/Multibranch-Pipeline.git')
            }
        }

        stage('Build') {
            steps {
                buildApp()
            }
        }

        stage('Docker Build') {
            steps {
                dockerBuild('sahilhinge89/multibranch-app', 'latest')
            }
        }

        stage('Docker Push') {
            steps {
                dockerPush('sahilhinge89/multibranch-app', 'latest')
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                deployK8s('k8s/deployment.yaml')
            }
        }

        stage('Helm Deploy') {
            steps {
                helmDeploy('my-app', './helm-chart', 'default')
            }
        }

        stage('Notify') {
            steps {
                slackNotify('Deployment successful!', '#deployments')
            }
        }
    }

    post {
        failure {
            rollback('my-app', 'default')
        }
        always {
            cleanup()
        }
    }
}
```

---

## ✅ Build Result

All 9 stages executed successfully end-to-end (Build **#11**, 5.5 seconds):

```
Checkout → Build → Docker Build → Docker Push → Deploy to Kubernetes
→ Helm Deploy → Notify → Post Actions (Cleanup)
```

Status: **SUCCESS** ✅

---

## 🛣️ Next Steps

To move from placeholder logic to a fully functional pipeline:

1. **Build** – Install Maven (or the appropriate build tool) on the Jenkins agent and restore real `mvn` command in `buildApp.groovy`.
2. **Docker Build/Push** – Ensure Docker is available on the agent (or use Docker-in-Docker / a Docker-enabled agent) and configure Docker Hub credentials for `docker login`.
3. **Deploy to Kubernetes** – Install `kubectl` on the agent and configure a kubeconfig with cluster access.
4. **Helm Deploy** – Install Helm on the agent and point it at a real Helm chart.
5. **Notify** – Install and configure the Jenkins Slack Notification plugin, then uncomment the `slackSend` call.
6. **Rollback** – Verify Helm rollback works against a real release once Helm is functional.

Each of these can be re-enabled one function at a time in the shared library repo, following the same stage-by-stage validation approach used to build this pipeline.