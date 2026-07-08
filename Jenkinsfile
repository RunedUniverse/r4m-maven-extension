def evalValue(expression, path = null) {
    def output = sh( returnStdout: true, script: """
            mvn-dev org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate \
                -Dexpression=${ expression } -q -DforceStdout \
                ${ path==null ? '' : ('-pl='+path) }
        """);
    def result = output.readLines().last();
    if (result.startsWith('[ERROR]'))
        error("[ERROR] Failed to get property » ${ expression } \n ${ output }")
    return result;
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
	def groupId = mod.metadata().get('maven.groupId');
	def artifactId = mod.metadata().get('maven.artifactId');
	def version = mod.metadata().get('maven.version');
	echo "Building: ${ groupId }:${ artifactId }:${ version }"
	try {
		sh "mvn-dev -P ${ REPOS },${ getToolchainId(mod) },ci-install -pl=${ relPath }"
	} finally {
		def baseName = "${ artifactId }-${ version }"
		// archive artifacts
		dir(path: "${ mod.path() }/target") {
			if(!fileExists("${ baseName }.pom")) {
				// create spec .pom in target/ path
				sh "cp -T '${ mod.path() }/pom.xml' '${ baseName }.pom'"
			}
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

def testArtifacts(mods, tag, toolchainId, testProfile, parent = null, properties = []) {
	mods = mods.findAll({ it.hasTag(tag) })
	stage(tag) {
		if(mods.isEmpty()) {
			skipStage()
			return
		}

		def modPaths = mods.collect({ it.relPathFrom(parent) }).join(',');
		def props = properties.collect({ "-D${ it }" }).join(' ');
		sh "mvn-dev -P ${ REPOS },${ toolchainId },ci-test-build -pl=${ modPaths } ${ props }"
		sh "mvn-dev --fail-never -P ${ REPOS },${ toolchainId },ci-test-exec,${ testProfile } -pl=${ modPaths } ${ props }"
		// check tests, archive reports in case junit flags errors
		junit '*/target/surefire-reports/*.xml'
		if(currentBuild.resultIsWorseOrEqualTo('UNSTABLE')) {
			archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
		}
		// clean up the test reports
		sh 'rm -R */target/surefire-reports/*'
	}
}

node( label: 'linux' ) {
	repoProxy(['maven>central': 'central', 'maven>runeduniverse>releases': 'rnet-releases', 'maven>runeduniverse>development': 'rnet-development']) {
	withModules {
		tool(name: 'maven-latest', type: 'maven')

		stage('Checkout SCM') {
			checkout2(scm)
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
			
			addModule id: 'r4m-parent',         path: '.',                  name: 'R4M Parent',                tags: [  ]
			addModule id: 'r4m-sources',        path: 'sources',            name: 'R4M Bill of Sources',       tags: [ 'bom', 'src' ]
			addModule id: 'r4m-model',          path: 'model',              name: 'R4M Model',                 tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-api',            path: 'api',                name: 'R4M API',                   tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-model-builder',  path: 'model-builder',      name: 'R4M Model Builder',         tags: [ 'pack-jar', 'jdk-1.8.0', 'src' ]
			addModule id: 'r4m-extension',      path: 'extension',          name: 'R4M Extension',             tags: [ 'pack-jar', 'jdk-1.8.0'        ]
		}
		def parentMod = getModule(id: 'r4m-parent');

		stage('Init Modules') {
			perModule(failFast: true) {
				def mod = getModule();
				def relPath = mod.relPathFrom(parentMod);
				mod.metadata().put('maven.groupId', evalValue('project.groupId', relPath));
				mod.metadata().put('maven.artifactId', evalValue('project.artifactId', relPath));
				def version = evalValue('project.version', relPath);
				mod.metadata().put('maven.version', version);
				// check skip flag
				// if not skipped -> check if this version already exists!
				mod.activate(!mod.hasTag('skip') && !gitTagExists2(scm: scm, tag: "${ mod.id() }/v${ version }"));
			}
		}
		stage ('Info') {
			sh 'printenv | sort'
		}

		stage('Update Maven Repo') {
			echo 'purging local maven repository'
			sh "mvn-dev -P ${ REPOS } dependency:purge-local-repository -DactTransitively=false -DreResolve=false"

			echo 'caching validation dependencies'
			sh "mvn-dev -P ${ REPOS },ci-validate dependency:resolve-plugins dependency:resolve -U --fail-never"

			if(checkAllModules(match: 'all', active: false)) {
				echo 'skipping build dependency download » unused'
			} else {
				echo 'caching build dependencies'
				sh "mvn-dev -P ${ REPOS },ci-install dependency:resolve -U --fail-never"
			}
		}

		bundleContext {
			stage('Install R4M Parent') {
				installArtifact( parentMod );
			}
			stage('Install - BOMs') {
				perModule(withTagIn: [ 'bom' ]) {
					installArtifact( getModule(), parentMod );
				}
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

			stage('Code Validation') {
				// note: bugged maven artifact resolve requires all modules to be locally installed before license verification
				sh "mvn-dev -P ${ REPOS },ci-validate --fail-at-end -T1C"
			}

			stage('Smoke Test') {
				def mods = getModules(withTags: [ 'test-smoke' ]);
				if(!mods.any({ it.active() })) {
					skipStage()
					return
				}

				echo 'force update bom version for tests -> test for possible collisions caused by this update'
				def props = [ /* set-bom-version-override=x.x.x */ ];

				testArtifacts(mods, 'jdk-1.8.0', 'toolchain-openjdk-1-8-0', 'test-smoke', parentMod, props);
			}

			// System Packages are on hold see the GitHub Issue:
			// https://github.com/RunedUniverse/r4m-maven-extension/issues/17
			/*
			stage('Build System Packages') {
				def modExt    = getModule(id: 'r4m-extension');
				def modSrc    = getModule(id: 'r4m-sources');
				stage('R4M Extension') {
					if(!modExt.active()) {
						skipStage()
						return
					}
					try {
						sh "mvn-dev -P ${ REPOS },pack-ext -pl=${ parentMod.relPathTo(modExt) }"
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
						sh "mvn-dev -P ${ REPOS },pack-lib -pl=${ parentMod.relPathTo(modExt) }"
					} finally {
						dir(path: "${ modExt.path() }/target") {
							// copy packages
							sh "cp *-lib-*.rpm ${ ARCHIVE_PATH }"
						}
					}
				}
			}
			*/

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
						def groupId = mod.metadata().get('maven.groupId');
						def artifactId = mod.metadata().get('maven.artifactId');
						def version = mod.metadata().get('maven.version');
						gitTagPush2(scm: scm, tag: "${ mod.id() }/v${ version }", comment: "[artifact] ${ groupId }:${ artifactId }:${ version }")
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
	}}
}
