def evalValue(expression, path) {
	return sh( returnStdout: true,
		script: "mvn-dev -P ${ env.REPOS } org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=${ expression } -q -DforceStdout -pl=${ path } | tail -1")
}

def getToolchainId(mod) {
	return 'toolchain-openjdk-1-8-0';
}

def installArtifact(mod, parent = null) {
	if(!mod.active()) {
		skipStage()
		return
	}
	def relPath = (parent == null ? '.' : mod.relPathFrom(parent))
	// get module metadata
	def groupId = evalValue('project.groupId', relPath)
	def artifactId = evalValue('project.artifactId', relPath)
	def version = evalValue('project.version', relPath)
	echo "Building: ${ groupId }:${ artifactId }:${ version }"
	try {
		sh "mvn-dev -P ${ REPOS },${ getToolchainId(mod) },install -pl=${ relPath }"
	} finally {
		def baseName = "${ artifactId }-${ version }"
		// create spec .pom in target/ path
		sh "cp -T '${ mod.path() }/pom.xml' '${ mod.path() }/target/${ baseName }.pom'"
		// archive artifacts
		dir(path: "${ mod.path() }/target") {
			sh 'ls -l'
			archiveArtifacts artifacts: "${ baseName }.pom", fingerprint: true
			if(mod.hasTag('pack-jar')) {
				archiveArtifacts artifacts: "${ baseName }*.jar", fingerprint: true
			}
		}
		// create signatures
		signArtifacts(artifacts: "${ baseName }*")
		// bundle artifacts + signatures
		bundleArtifacts( bundle: mod.id(), artifacts: "${ baseName }.pom*", metadata: [
			'groupId': groupId, 'artifactId': artifactId, 'version': version
		])
		for (test in [ false, true ]) {
			for (classifier in [ '', 'javadoc', 'sources' ]) {
				if(test)
					classifier = classifier=='' ? 'tests' : ('test-'+classifier)
				bundleArtifacts( bundle: mod.id(), artifacts: "${ baseName }${ classifier=='' ? '' : ('-'+classifier) }.jar*", metadata: [
					'groupId': groupId, 'artifactId': artifactId, 'version': version, 'classifier': classifier
				])
			}
		}
	}
}

node( label: 'linux' ) {
	withModules {
		tool(name: 'maven-latest', type: 'maven')

		stage('Checkout SCM') {
			checkout(scm)
		}

		sh 'chmod +x $WORKSPACE/.build/*'
		env.setProperty('PATH+SCRIPTS', "${ env.WORKSPACE }/.build")
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
			
			addModule id: 'r4m-parent',         path: '.',                  name: 'R4M Parent',                tags: [ 'parent'      ]
			addModule id: 'r4m-sources',        path: 'sources',            name: 'R4M Bill of Sources',       tags: [ 'sbom', 'src' ]
			addModule id: 'r4m-model',          path: 'model',              name: 'R4M Model',                 tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-api',            path: 'api',                name: 'R4M API',                   tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-model-builder',  path: 'model-builder',      name: 'R4M Model Builder',         tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-extension',      path: 'extension',          name: 'R4M Extension',             tags: [ 'pack-jar', 'jdk-1.8.0'        ]
		}
		def parentMod = getModule(id: 'r4m-parent');

		stage('Init Modules') {
			sshagent (credentials: ['RunedUniverse-Jenkins']) {
				perModule(failFast: true) {
					def mod = getModule();
					// check skip flag
					def active = !mod.hasTag('skip');
					// if not skipped -> check if this version already exists!
					if(active) {
						def version = evalValue('project.version', mod.relPathFrom('r4m-parent'))
						active = sh(
								label: "check if git tag \"${ mod.id() }/v${ version }\" exists",
								returnStatus: true,
								script: "git ls-remote --tags --exit-code origin refs/tags/${ mod.id() }/v${ version } &>/dev/null"
							) != 0
					}
					mod.activate( active );
				}
			}
		}
		stage ('Info') {
			sh 'printenv | sort'
		}

		stage('Update Maven Repo') {
			echo 'purging local maven repository'
			sh "mvn-dev -P ${ REPOS } dependency:purge-local-repository -DactTransitively=false -DreResolve=false"

			echo 'caching validation dependencies'
			sh "mvn-dev -P ${ REPOS },validate dependency:resolve-plugins dependency:resolve -U --fail-never"

			if(checkAllModules(match: 'all', active: false)) {
				echo 'skipping build dependency download » unused'
			} else {
				echo 'caching build dependencies'
				sh "mvn-dev -P ${ REPOS },install dependency:resolve -U --fail-never"
			}
		}

		stage('Code Validation') {
			sh "mvn-dev -P ${ REPOS },validate --fail-at-end -T1C"
		}

		bundleContext {
			stage('Install R4M Parent') {
				installArtifact( parentMod );
			}
			stage('Install R4M Bill of Sources') {
				installArtifact( getModule(id: 'r4m-sources'), parentMod );
			}

			stage('Install R4M Model') {
				installArtifact( getModule(id: 'r4m-model'), parentMod );
			}
			stage('Install R4M API') {
				installArtifact( getModule(id: 'r4m-api'), parentMod );
			}
			stage('Install R4M Model Builder') {
				installArtifact( getModule(id: 'r4m-model-builder'), parentMod );
			}
			stage('Install R4M Extension') {
				installArtifact( getModule(id: 'r4m-extension'), parentMod );
			}

			// System Packages are on hold see the GitHub Issue:
			// https://github.com/RunedUniverse/r4m-maven-extension/issues/17
			/*
			stage('Build System Packages') {
				def modParent = getModule(id: 'r4m-parent');
				def modExt    = getModule(id: 'r4m-extension');
				def modSrc    = getModule(id: 'r4m-sources');
				stage('R4M Extension') {
					if(!modExt.active()) {
						skipStage()
						return
					}
					try {
						sh "mvn-dev -P ${ REPOS },pack-ext -pl=${ modParent.relPathTo(modExt) }"
					} finally {
						dir(path: "${ modExt.path() }/target") {
							// copy packages
							sh "cp *.rpm ${ ARCHIVE_PATH }"
						}
					}
				}
				stage('R4M Library') {
					if(!checkAllModules(withTagIn: [ 'src' ], active: true)) {
						skipStage()
						return
					}
					try {
						sh "mvn-dev -P ${ REPOS },pack-lib -pl=${ modParent.relPathTo(modExt) }"
					} finally {
						dir(path: "${ modExt.path() }/target") {
							// copy packages
							sh "cp *-lib-*.rpm ${ ARCHIVE_PATH }"
						}
					}
				}
			}
			*/

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
					unarchive mapping: ['*':'.']
					sh 'ls -l'
					sh "tar -I \"pxz -9\" -cvf ${ ARCHIVE_PATH }r4m-maven-extension.tar.xz *"
					sh "zip -9 ${ ARCHIVE_PATH }r4m-maven-extension.zip *"
				}
				dir(path: "${ env.ARCHIVE_PATH }") {
					archiveArtifacts artifacts: '*', fingerprint: true
				}
			}

			stage('Deploy') {
				perModule {
					def mod = getModule();
					if(!mod.active()) {
						skipStage()
						return
					}
					// bundle info
					bundleInfo( bundle: mod.id(), metadata: true )
					// deploy to development repo
					stage('Develop'){
						deployArtifacts( bundle: mod.id(), repo: 'nexus-runeduniverse>maven-development' )
					}
					// deploy to release repo
					stage('Release') {
						if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE') || env.BRANCH_NAME != 'master') {
							skipStage()
							return
						}
						deployArtifacts( bundle: mod.id(), repo: 'nexus-runeduniverse>maven-releases' )
						def groupId = evalValue('project.groupId', mod.relPathFrom(parentMod))
						def artifactId = evalValue('project.artifactId', mod.relPathFrom(parentMod))
						def version = evalValue('project.version', mod.relPathFrom(parentMod))
						sshagent (credentials: ['RunedUniverse-Jenkins']) {
							sh "git tag -a ${ mod.id() }/v${ version } -f -m '[artifact] ${ groupId }:${ artifactId }:${ version }'"
							sh "git push origin ${ mod.id() }/v${ version }"
						}
					}
					// merge bundles into default
					bundleMerge( source: mod.id() )
				}
				stage('Stage at Maven-Central') {
					if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE') || env.BRANCH_NAME != 'master') {
						skipStage()
						return
					}
					deployArtifacts( repo: 'maven-central>net.runeduniverse' )
				}
			}
		}

		cleanWs()
	}
}
