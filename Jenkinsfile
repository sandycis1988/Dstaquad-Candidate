pipeline {
    agent any
    environment {
        DOCKER_REGISTRY = "docker.io"
        DUBERHUB_REPO = "sandycis476"
        IMAGE_NAME = "candidate"
        KUBE_NAMESPACE = "ingress-nginx"
        DOCKER_CREDENTIALS_ID = "docker-hub"
        KUBECONFIG_CREDENTIALS_ID = "k8s-service-account-token"
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
                    dockerImage = docker.build("${DUBERHUB_REPO}/${IMAGE_NAME}:${env.BUILD_ID}")
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                sh 'docker run ${DUBERHUB_REPO}/${IMAGE_NAME}:${env.BUILD_ID} npm test'
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }
        
        stage('Update K8s Manifests') {
            steps {
                script {
                    sh """
                        sed -i 's|image:.*|image: ${DUBERHUB_REPO}/${IMAGE_NAME}:${env.BUILD_ID}|g' k8s/deployment.yaml
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
