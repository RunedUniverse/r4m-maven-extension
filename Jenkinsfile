pipeline {
	agent any
	options {
		throttleJobProperty(
			categories: ['runeduniverse-rogm'],
			throttleEnabled: true,
			throttleOption: 'category'
		)
	}
	tools {
		maven 'maven-latest'
	}
	environment {
		PATH = """${sh(
				returnStdout: true,
				script: 'chmod +x $WORKSPACE/.build/*; printf $WORKSPACE/.build:$PATH'
			)}"""

		GLOBAL_MAVEN_SETTINGS = """${sh(
				returnStdout: true,
				script: 'printf /srv/jenkins/.m2/global-settings.xml'
			)}"""
		MAVEN_SETTINGS = """${sh(
				returnStdout: true,
				script: 'printf $WORKSPACE/.mvn/settings.xml'
			)}"""
		MAVEN_TOOLCHAINS = """${sh(
				returnStdout: true,
				script: 'printf $WORKSPACE/.mvn/toolchains.xml'
			)}"""
		REPOS = """${sh(
				returnStdout: true,
				script: 'REPOS=repo-releases; if [ $GIT_BRANCH != master ]; then REPOS=$REPOS,repo-development; fi; printf $REPOS'
			)}"""

		CHANGES_R4M_PARENT = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change pom.xml r4m-parent'
			)}"""
		CHANGES_R4M_SOURCES = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change sources/pom.xml r4m-sources'
			)}"""
		CHANGES_R4M_MODEL = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change model/pom.xml r4m-model'
			)}"""
		CHANGES_R4M_API = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-api/pom.xml r4m-api'
			)}"""
		CHANGES_R4M_MODEL_BUILDER = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change model-builder/pom.xml r4m-model-builder'
			)}"""
		CHANGES_R4M_EXTENSION = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-extension/pom.xml r4m-extension'
			)}"""
	}
	stages {
		stage('Initialize') {
			steps {
				sh 'echo "PATH = ${PATH}"'
				sh 'echo "M2_HOME = ${M2_HOME}"'
				sh 'printenv | sort'
			}
		}
		stage('Update Maven Repo') {
			when {
				anyOf {
					environment name: 'CHANGES_R4M_PARENT', value: '1'
					environment name: 'CHANGES_R4M_SOURCES', value: '1'
					environment name: 'CHANGES_R4M_MODEL', value: '1'
					environment name: 'CHANGES_R4M_API', value: '1'
					environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
					environment name: 'CHANGES_R4M_EXTENSION', value: '1'
				}
			}
			steps {
				sh 'mvn-dev -P ${REPOS} dependency:purge-local-repository -DactTransitively=false -DreResolve=false --non-recursive'
				sh 'mvn-dev -P ${REPOS} dependency:resolve --non-recursive'
				sh 'mkdir -p target/result/'
			}
		}
		stage('Install R4M Parent') {
			when {
				environment name: 'CHANGES_R4M_PARENT', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
				sh 'mkdir -p target/result/'
				sh 'ls -l target/'
			}
			post {
				always {
					dir(path: 'target') {
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.asc result/'
					}
				}
			}
		}
		stage('Install R4M Bill of Sources') {
			when {
				environment name: 'CHANGES_R4M_SOURCES', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS} dependency:resolve -pl sources'
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl sources'
			}
			post {
				always {
					dir(path: 'sources/target') {
						sh 'ls -l'
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.asc ../../target/result/'
					}
				}
			}
		}
		stage('Build R4M Model') {
			when {
				environment name: 'CHANGES_R4M_MODEL', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl model'
			}
			post {
				always {
					dir(path: 'model/target') {
						sh 'ls -l'
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.jar *.asc ../../target/result/'
					}
				}
			}
		}
		stage('Install R4M API') {
			when {
				environment name: 'CHANGES_R4M_API', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl api'
			}
			post {
				always {
					dir(path: 'api/target') {
						sh 'ls -l'
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.asc ../../target/result/'
					}
				}
			}
		}
		stage('Build R4M Model Builder') {
			when {
				environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl model-builder'
			}
			post {
				always {
					dir(path: 'model-builder/target') {
						sh 'ls -l'
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.jar *.asc ../../target/result/'
					}
				}
			}
		}
		stage('Build R4M Extension') {
			when {
				environment name: 'CHANGES_R4M_EXTENSION', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl extension'
			}
			post {
				always {
					dir(path: 'extension/target') {
						sh 'ls -l'
						archiveArtifacts artifacts: '*.pom', fingerprint: true
						archiveArtifacts artifacts: '*.asc', fingerprint: true
						sh 'cp *.pom *.jar *.asc ../../target/result/'
					}
				}
			}
		}

		stage('License Check') {
			when {
				anyOf {
					environment name: 'CHANGES_R4M_PARENT', value: '1'
					environment name: 'CHANGES_R4M_SOURCES', value: '1'
					environment name: 'CHANGES_R4M_MODEL', value: '1'
					environment name: 'CHANGES_R4M_API', value: '1'
					environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
					environment name: 'CHANGES_R4M_EXTENSION', value: '1'
				}
			}
			steps {
				sh 'mvn-dev -P ${REPOS},license-check,license-apache2-approve'
			}
		}

		//stage('System Test') {
		//	when {
		//		anyOf {
		//			environment name: 'CHANGES_R4M_PARENT', value: '1'
		//			environment name: 'CHANGES_R4M_SOURCES', value: '1'
		//			environment name: 'CHANGES_R4M_MODEL', value: '1'
		//			environment name: 'CHANGES_R4M_API', value: '1'
		//			environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
		//			environment name: 'CHANGES_R4M_EXTENSION', value: '1'
		//		}
		//	}
		//	steps {
		//		sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,test-junit-jupiter,test-system'
		//	}
		//	post {
		//		success {
		//			junit '*/target/surefire-reports/*.xml'
		//		}
		//		failure {
		//			junit '*/target/surefire-reports/*.xml'
		//			archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
		//		}
		//	}
		//}

		stage('Package Build Result') {
			when {
				anyOf {
					environment name: 'CHANGES_R4M_PARENT', value: '1'
					environment name: 'CHANGES_R4M_SOURCES', value: '1'
					environment name: 'CHANGES_R4M_MODEL', value: '1'
					environment name: 'CHANGES_R4M_API', value: '1'
					environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
					environment name: 'CHANGES_R4M_EXTENSION', value: '1'
				}
			}
			steps {
				dir(path: 'target/result') {
					sh 'ls -l'
					sh 'tar -I "pxz -9" -cvf ../r4m-maven-extension.tar.xz *'
					sh 'zip -9 ../r4m-maven-extension.zip *'
				}
			}
			post {
				always {
					dir(path: 'target') {
						archiveArtifacts artifacts: '*.tar.xz', fingerprint: true
						archiveArtifacts artifacts: '*.zip', fingerprint: true
					}
				}
			}
		}

		stage('Deploy') {
			parallel {
				stage('Develop') {
					stages {
						stage('r4m-parent') {
							when {
								environment name: 'CHANGES_R4M_PARENT', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy --non-recursive'
							}
						}
						stage('r4m-sources') {
							when {
								environment name: 'CHANGES_R4M_SOURCES', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl sources'
							}
						}
						stage('r4m-model') {
							when {
								environment name: 'CHANGES_R4M_MODEL', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl model'
							}
						}
						stage('r4m-api') {
							when {
								environment name: 'CHANGES_R4M_API', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl api'
							}
						}
						stage('r4m-model-builder') {
							when {
								environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl model-builder'
							}
						}
						stage('r4m-extension') {
							when {
								environment name: 'CHANGES_R4M_EXTENSION', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl extension'
							}
						}
					}
				}

				stage('Release') {
					when {
						branch 'master'
					}
					stages {
						stage('r4m-parent') {
							when {
								environment name: 'CHANGES_R4M_PARENT', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-pom-signed --non-recursive'
							}
						}
						stage('r4m-sources') {
							when {
								environment name: 'CHANGES_R4M_SOURCES', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-pom-signed -pl sources'
							}
						}
						stage('r4m-model') {
							when {
								environment name: 'CHANGES_R4M_MODEL', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl model'
							}
						}
						stage('r4m-api') {
							when {
								environment name: 'CHANGES_R4M_API', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl api'
							}
						}
						stage('r4m-model-builder') {
							when {
								environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl model-builder'
							}
						}
						stage('r4m-extension') {
							when {
								environment name: 'CHANGES_R4M_EXTENSION', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl extension'
							}
						}
					}
				}
			}
		}

		stage('Stage at Maven-Central') {
			when {
				branch 'master'
			}
			stages {
				// never add : -P ${REPOS} => this is ment to fail here
				stage('r4m-parent') {
					when {
						environment name: 'CHANGES_R4M_PARENT', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-pom-signed --non-recursive'
					}
				}
				stage('r4m-sources') {
					when {
						environment name: 'CHANGES_R4M_SOURCES', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-pom-signed -pl sources'
					}
				}
				stage('r4m-model') {
					when {
						environment name: 'CHANGES_R4M_MODEL', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl model'
					}
				}
				stage('r4m-api') {
					when {
						environment name: 'CHANGES_R4M_API', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl api'
					}
				}
				stage('r4m-model-builder') {
					when {
						environment name: 'CHANGES_R4M_MODEL_BUILDER', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl model-builder'
					}
				}
				stage('r4m-extension') {
					when {
						environment name: 'CHANGES_R4M_EXTENSION', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl extension'
					}
				}
			}
		}
	}
	post {
		cleanup {
			cleanWs()
		}
	}
}
