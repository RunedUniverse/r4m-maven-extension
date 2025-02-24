def installArtifact(modId) {
	def mod = getModule(id: modId);
	if(!mod.active()) {
		skipStage()
		return
	}
	try {
		sh "mvn-dev -P ${ REPOS },toolchain-openjdk-1-8-0,install -pl=${ mod.relPathFrom('r4m-parent') }"
	} finally {
		dir(path: "${ mod.path() }/target") {
			sh 'ls -l'
			// copy pom & signatures
			sh "cp *.pom *.asc ${ RESULT_PATH }"
			// copy packaging specific files
			if(mod.hasTag('pack-jar')) {
				sh "cp *.jar ${ RESULT_PATH }"
			}
			if(mod.hasTag('pack-rpm')) {
				sh "cp *.rpm ${ ARCHIVE_PATH }"
			}
		}
	}
}

node {
	withModules {
		tool(name: 'maven-latest', type: 'maven')

		stage('Checkout SCM') {
			checkout(scm)
		}

		sh 'chmod +x $WORKSPACE/.build/*'
		env.setProperty('PATH+SCRIPTS', "${ env.WORKSPACE }/.build")
		env.GLOBAL_MAVEN_SETTINGS     = '/srv/jenkins/.m2/global-settings.xml'
		env.MAVEN_SETTINGS            = "${ env.WORKSPACE }/.mvn/settings.xml"
		env.MAVEN_TOOLCHAINS          = "${ env.WORKSPACE }/.mvn/toolchains.xml"
		if(env.GIT_BRANCH == 'master') {
			env.REPOS = 'repo-releases'
		} else {
			env.REPOS = 'repo-releases,repo-development'
		}

		stage('Initialize') {
			env.RESULT_PATH  = "${ WORKSPACE }/result/"
			env.ARCHIVE_PATH = "${ WORKSPACE }/archive/"
			sh "mkdir -p ${ RESULT_PATH }"
			sh "mkdir -p ${ ARCHIVE_PATH }"
			
			addModule id: 'r4m-parent',         path: '.',                  name: 'R4M Parent',                tags: [ 'pack-pom', 'parent'   ]
			addModule id: 'r4m-sources',        path: 'sources',            name: 'R4M Bill of Sources',       tags: [ 'pack-pom', 'bom'      ]
			addModule id: 'r4m-model',          path: 'model',              name: 'R4M Model',                 tags: [ 'pack-jar'             ]
			addModule id: 'r4m-api',            path: 'api',                name: 'R4M API',                   tags: [ 'pack-jar'             ]
			addModule id: 'r4m-model-builder',  path: 'model-builder',      name: 'R4M Model Builder',         tags: [ 'pack-jar'             ]
			addModule id: 'r4m-extension',      path: 'extension',          name: 'R4M Extension',             tags: [ 'pack-jar', 'pack-rpm' ]
		}

		stage('Init Modules') {
			sshagent (credentials: ['RunedUniverse-Jenkins']) {
				perModule(failFast: true) {
					module.activate(
						!module.hasTag('skip') && sh(
								returnStdout: true,
								script: "git-check-version-tag ${ module.id() } ${ module.relPathFrom('r4m-parent') }"
							) == '1'
					);
				}
			}
		}
		stage ('Info') {
			sh 'printenv | sort'
		}

		stage('Update Maven Repo') {
			if(checkAllModules(match: 'all', active: false)) {
				skipStage()
				return
			}
			sh "mvn-dev -P ${ REPOS } dependency:purge-local-repository -DactTransitively=false -DreResolve=false --non-recursive"
			sh "mvn-dev -P ${ REPOS } dependency:resolve-plugins -U"
		}

		stage('Code Validation') {
			perModule {
				if(!module.active()) {
					skipStage()
					return
				}
				sh "mvn-dev -P ${ REPOS },validate -pl=${ module.relPathFrom('r4m-parent') }"
			}
		}

		stage('Install R4M Parent') {
			def mod = getModule(id: 'r4m-parent');
			if(!mod.active()) {
				skipStage()
				return
			}
			try {
				sh "mvn-dev -P ${ REPOS },toolchain-openjdk-1-8-0,install --non-recursive"
			} finally {
				dir(path: "${ mod.path() }/target") {
					sh 'ls -l'
					sh "cp *.pom *.asc ${ RESULT_PATH }"
				}
			}
		}
		stage('Install R4M Bill of Sources') {
			installArtifact('r4m-sources');
		}

		stage('Install R4M Model') {
			installArtifact('r4m-model');
		}
		stage('Install R4M API') {
			installArtifact('r4m-api');
		}
		stage('Install R4M Model Builder') {
			installArtifact('r4m-model-builder');
		}
		stage('Install R4M Extension') {
			installArtifact('r4m-extension');
		}

		stage('Test') {
			if(!checkAllModules(withTagIn: [ 'test' ], active: true)) {
				skipStage()
				return
			}
			sh "mvn-dev -P ${ REPOS },toolchain-openjdk-1-8-0,build-tests"
			sh "mvn-dev --fail-never -P ${ REPOS },toolchain-openjdk-1-8-0,test-junit-jupiter,test-system"
			// check tests, archive reports in case junit flags errors
			junit '*/target/surefire-reports/*.xml'
			if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE')) {
				archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
			}
		}

		stage('Package Build Result') {
			if(checkAllModules(match: 'all', active: false)) {
				skipStage()
				return
			}
			dir(path: "${ env.RESULT_PATH }") {
				sh 'ls -l'
				archiveArtifacts artifacts: '*', fingerprint: true
				sh "tar -I \"pxz -9\" -cvf ${ ARCHIVE_PATH }r4m-maven-extension.tar.xz *"
				sh "zip -9 ${ ARCHIVE_PATH }r4m-maven-extension.zip *"
			}
			dir(path: "${ env.ARCHIVE_PATH }") {
				archiveArtifacts artifacts: '*', fingerprint: true
			}
		}

		stage('Deploy') {
			perModule {
				def mod = module;
				if(!mod.active()) {
					skipStage()
					return
				}
				def deployProfilePrefix = 'deploy';
				if(mod.hasTag('pack-pom')) {
					deployProfilePrefix = 'deploy-pom';
				}
				stage('Develop'){
					sh "mvn-dev -P ${ REPOS },dist-repo-development,${ deployProfilePrefix } -pl=${ mod.relPathFrom('r4m-parent') }"
				}
				stage('Release') {
					if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE') || env.GIT_BRANCH != 'master') {
						skipStage()
						return
					}
					sh "mvn-dev -P ${ REPOS },dist-repo-releases,${ deployProfilePrefix }-signed -pl=${ mod.relPathFrom('r4m-parent') }"
				}
				stage('Stage at Maven-Central') {
					if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE') || env.GIT_BRANCH != 'master') {
						skipStage()
						return
					}
					// never add : -P ${REPOS} => this is ment to fail here
					sh "mvn-dev -P repo-releases,dist-repo-maven-central,${ deployProfilePrefix }-signed -pl=${ mod.relPathFrom('r4m-parent') }"
					sshagent (credentials: ['RunedUniverse-Jenkins']) {
						sh "git push origin \$(git-create-version-tag ${ mod.id() } ${ mod.relPathFrom('r4m-parent') })"
					}
				}
			}
		}

		cleanWs()
	}
}
