pipeline {
    agent any
    environment {
        DOCKER_REGISTRY = "docker.io"
        DOCKERHUB_REPO = "sandycis476"
        IMAGE_NAME = "candidate"
        KUBE_NAMESPACE = "ingress-nginx"
        DOCKER_CREDENTIALS_ID = "docker-hub"
        KUBECONFIG_CREDENTIALS_ID = "k8s-service-account-token"
        GOOGLE_CREDENTIALS = 'k8s-service-account-token' // Jenkins credentials ID for Google Cloud JSON key
        GCP_PROJECT = 'proud-outpost-447109-m8'
        GKE_CLUSTER = 'k8s-dataquad-dev'
        GKE_ZONE = 'us-central1-c' // e.g., us-central1-a
    }
    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'master', 
                url: 'https://github.com/sandycis1988/Dstaquad-Candidate.git'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                    docker build -t ${DOCKERHUB_REPO}/${IMAGE_NAME}:${BUILD_ID} .
                    """
                }
            }
        }
        
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    sh """
                    echo $DOCKERHUB_REPO/$IMAGE_NAME:$BUILD_ID
                    docker login -u ${env.DOCKERHUB_REPO} -p ${env.DOCKER_CREDENTIALS_ID} 
                    docker push ${DOCKERHUB_REPO}/${IMAGE_NAME}:${BUILD_ID}
                    docker tag ${DOCKERHUB_REPO}/${IMAGE_NAME}:${BUILD_ID} ${DOCKERHUB_REPO}/${IMAGE_NAME}:latest
                    docker push ${DOCKERHUB_REPO}/${IMAGE_NAME}:latest
                    """
                }
            }
        }
        
        stage('Update K8s Manifests') {
            steps {
                script {
                    sh """
                        sed -i 's|image:.*|image: ${DOCKERHUB_REPO}/${IMAGE_NAME}:${env.BUILD_ID}|g' k8s/deployment.yaml
                    """
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: KUBECONFIG_CREDENTIALS_ID]) {
                    sh """
                        kubectl apply -f k8s/deployment.yaml -n ${KUBE_NAMESPACE}
                        kubectl apply -f k8s/service.yaml -n ${KUBE_NAMESPACE}
                        kubectl rollout status deployment/${IMAGE_NAME} -n ${KUBE_NAMESPACE} --timeout=2m
                    """
                }
            }
        }
    }
}
