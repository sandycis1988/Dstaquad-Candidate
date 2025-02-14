pipeline {
    agent any

    environment {
        // Define your Docker Hub image name
        DOCKER_IMAGE = "sandycis476/candidate"
        // Define your Kubernetes namespace
        KUBE_NAMESPACE = "ingress-nginx"
        // Define your Kubernetes deployment name
        DEPLOYMENT_NAME = "candidate-api"
        // Define your Docker Hub credentials ID (stored in Jenkins)
        DOCKER_HUB_CREDENTIALS_ID = "docker-hub"
        // Define your Kubernetes Service Account token credentials ID (stored in Jenkins)
        K8S_SERVICE_ACCOUNT_CREDENTIALS_ID = "k8s-service-account-token"
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Checkout your repository (if needed)
                git branch: 'master', url: 'https://github.com/sandycis1988/Dstaquad-Candidate.git'
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    // Authenticate with Docker Hub
                    withCredentials([usernamePassword(credentialsId: DOCKER_HUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        """
                    }
                }
            }
        }

        stage('Pull Latest Docker Image') {
            steps {
                script {
                    // Pull the latest image from Docker Hub
                    sh """
                        docker pull ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    // Use the Kubernetes Service Account token for authentication
                    withCredentials([string(credentialsId: K8S_SERVICE_ACCOUNT_CREDENTIALS_ID, variable: 'K8S_TOKEN')]) {
                        sh """
                            # Set up kubectl with the Service Account token
                            kubectl config set-credentials jenkins --token=${K8S_TOKEN}
                            kubectl config set-cluster my-cluster --server=https://<your-cluster-api-server> --insecure-skip-tls-verify=true
                            kubectl config set-context jenkins-context --cluster=my-cluster --user=jenkins
                            kubectl config use-context jenkins-context

                            # Apply the Kubernetes deployment YAML
                            cat <<EOF | kubectl apply -f -
                            apiVersion: apps/v1
                            kind: Deployment
                            metadata:
                              name: ${DEPLOYMENT_NAME}
                              namespace: ${KUBE_NAMESPACE}
                            spec:
                              replicas: 1
                              selector:
                                matchLabels:
                                  app: ${DEPLOYMENT_NAME}
                              template:
                                metadata:
                                  labels:
                                    app: ${DEPLOYMENT_NAME}
                                spec:
                                  containers:
                                  - name: ${DEPLOYMENT_NAME}
                                    image: ${DOCKER_IMAGE}:latest
                                    ports:
                                    - containerPort: 80
                            EOF
                        """
                    }
                }
            }
        }

        stage('Clean Up Old Docker Images') {
            steps {
                script {
                    // Keep only the last 5 images in Docker Hub (manual cleanup example)
                    sh """
                        # List all image tags, sort by creation date, and keep only the last 5
                        docker images ${DOCKER_IMAGE} --format "{{.ID}} {{.CreatedAt}}" | sort -rk2 | awk "NR>5 {print $1}"" | xargs -r docker rmi -f
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Deployment successful!"
        }
        failure {
            echo "Deployment failed. Check logs for more details."
        }
    }
}
