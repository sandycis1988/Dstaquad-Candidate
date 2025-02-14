pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "sandycis476/candidate"
        DOCKER_TAG = "latest"
        DOCKER_CREDS = credentials('docker-hub')
        KUBECONFIG = credentials('k8s-service-account-token')
        DOCKER_CONFIG = "${WORKSPACE}/.docker" // Custom Docker config directory
    }

    stages {
        stage('Prepare Docker Config') {
            steps {
                script {
                    // Create a custom Docker config directory
                    sh """
                        mkdir -p ${DOCKER_CONFIG}
                        echo '{}' > ${DOCKER_CONFIG}/config.json
                    """
                }
            }
        }

        stage('Checkout GitHub Repository') {
            steps {
                git branch: 'master', url: 'https://github.com/sandycis1988/Dstaquad-Candidate.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh """
                        kubectl apply -f deployment.yaml
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
