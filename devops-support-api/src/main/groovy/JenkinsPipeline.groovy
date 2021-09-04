import groovy.json.JsonSlurper

def getObjMap() {
	return [:];
}

pipeline {
    agent none
    environment {
        uniquePipelineId = 'MVN_RELEASE_1'
		vcsSelection = null;
		vcsType = null;
        repoId = -1;
        repoUrl = '';
		repoTabBase = '';
        repoTag = '';
        backEndBaseUrl = 'http://127.0.0.1:8888/DevOpsSupport/jenkins/';
		pipelineWorkspace = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/workspace"
		mvnBin = 'mvn'
        mvnOpts = "-gs /opt/scm/maven/globalSettings.xml -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.repo.local=./repository"
		svnCredentialsId = 'RCNET_SVN'
		gitCredentialsId = 'RCNET_GIT'
		developerGroup = 'developer'
		approverGroup = 'approver'
		prodReleaseMsg = 'Please enter the Production Ticket ID in order to continue. You are responsible to ensure the Ticket ID is valid at the moment of deployment.'
		environmentGroup = '';
    }
	
    stages {
        stage('VCS Selection') {
            steps {
                    script {
						def objMap = [:]
						def repoOptions = []
						vcsType = input(message: 'VCS Type', ok: 'Continue', submitter: developerGroup, parameters: [choice(choices: ["SVN","GIT"], description: 'Select VCS Repository Type', name: 'VCS_TYPE') ])
						def vcsRepoUrl = backEndBaseUrl + 'vcs/repository/json/'
						if(vcsType == 'SVN') {
							vcsRepoUrl = vcsRepoUrl + 1
						} else {
							vcsRepoUrl = vcsRepoUrl + 2
						}
							
						def response = httpRequest(url: vcsRepoUrl, httpMode: 'GET', contentType: 'APPLICATION_JSON')
                        def json = new JsonSlurper().parseText(response.content)
                        def repoList = json
						repoList.each { obj ->
							objMap.put(obj.id, [ "$obj.id", "$obj.url", "$obj.description", "$obj.tagBase", "$obj.environmentGroup" ])
							repoOptions.add(obj.id + " || " + obj.description)
						}
						
						// To avoid issues serializing JSON object, these need to be set to null
                        json = null
						repoList = null

                        vcsSelection = input(message: 'Required Pipeline Parameters', ok: 'Proceed', submitter: developerGroup, submitterParameter: 'submitter', parameters: [choice(choices: repoOptions, description: 'Select VCS Repository', name: 'VCS_URL'), string(name:'VCS_TAG', defaultValue:'v0.1.1', description: 'Enter Tag/Branch:'), booleanParam(name:'SkipTests', description:'Maven Options', defaultValue: false), booleanParam(name:'SkipJavaDocs', defaultValue: true), text(name:'RELEASE_COMMENTS', defaultValue:'JIRA-1234: Changes in App.', description: 'Enter Release Comments:') ])
                        def tmp = vcsSelection['VCS_URL'].replace(' || ','|')
                        repoId = tmp.substring(0, tmp.indexOf('|')) as int
                        /*repoUrl = tmp.substring(tmp.indexOf('|') + 1, tmp.length())*/
						repoUrl = objMap.get(repoId)[1]
                        repoTag = vcsSelection['VCS_TAG']
						repoTagBase = objMap.get(repoId)[3]
						environmentGroup = objMap.get(repoId)[4]
						
						if(vcsSelection['SkipTests']) {
							mvnOpts = mvnOpts + ' -DskipTests'
						}
						
						if(vcsSelection['SkipJavaDocs']) {
							mvnOpts = mvnOpts + ' -Dmaven.javadoc.skip=true'
						}

						echo 'mvnOpts=' + mvnOpts
						
						// This will replace the build id in blueocean, I don't think we want that now...
						//currentBuild.displayName = vcsSelection['RELEASE_COMMENTS']
						
						// The description will be displayed in the comments sections
						currentBuild.description = "[" + vcsSelection['submitter'] + "] " + vcsSelection['RELEASE_COMMENTS']

                        //echo 'VCS Repository Selection: [' + repoUrl + '] / Tag: [' + repoTag + '] / [' + repoId + "] / " + objMap.get(repoId)
						def reqBody = '{ "comments" : "' + vcsSelection['RELEASE_COMMENTS'] + '" , "tagName" : "' + vcsSelection["VCS_TAG"] + '" }'
						//echo reqBody
                        httpRequest(url: backEndBaseUrl + 'register/'+ vcsSelection['submitter'] + '/' + repoId + '/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: reqBody )
                    }

            }
        }
		
		/* Code checkout from VCS */
        stage('VCS Checkout') {
            agent {
                    node {
                    label 'master'
                    customWorkspace pipelineWorkspace
                }
            }
            steps {
                script {
                    echo 'Checking out from [' + vcsType + '], URL [' + repoUrl + repoTagBase + repoTag + ']'
	                if( vcsType == 'SVN') {
						checkout scm: [$class: 'SubversionSCM',
							additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '',	excludedRevprop: '',
							excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '',
							locations: [[credentialsId: svnCredentialsId, depthOption: 'infinity', local: '.', ignoreExternalsOption: true, remote: repoUrl + repoTagBase + repoTag]],
							workspaceUpdater: [$class: 'UpdateUpdater']]
					} else {
						checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: repoUrl]], branches: [[name: 'refs/tags/' + repoTag]]], poll: false
						/* ,credentialsId: 'RCNET_GIT' */
					}
                }
            }
        }
		
		/* Here is where the MAVEN project will be compiled and the list of artifacts to be deployed are identified. */
        stage('Project Build') {
            agent {
                    node {
                    label 'master'
                    customWorkspace pipelineWorkspace
                }
            }
            steps {
                echo 'Compiling Source Code [' + env.JOB_BASE_NAME + '], Internal Release ID [' + uniquePipelineId + ']'
                sh mvnBin + ' ' + mvnOpts + ' -B compile'
                script {
                    echo 'Seaching Maven Artifacts...'
                    def mvnBinaries = sh (returnStdout: true, script: mvnBin + ' ' + mvnOpts + ' -q -Dexec.executable=\'echo\' -Dexec.args=\'${project.groupId} ${project.artifactId} ${project.version} ${project.packaging}\' exec:exec').trim()
                    echo 'Maven Binaries Detected: ' + mvnBinaries
                    httpRequest(url: backEndBaseUrl + 'register/maven/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: mvnBinaries )
                }
            }
        }

		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* DEV Release Flow */
        stage('DEV Release Approval') {
			steps {
				echo 'Confirm DEV Release'
				script {
					def stepApproval = input(message: 'Approve Release to DEV?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
					echo stepApproval + " approved DEV release."
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated DEV Release.")
				}
	
			}
		}
        stage('DEV Release Process') {
            agent {
                    node {
                    label 'master'
                    customWorkspace pipelineWorkspace
                }
            }
            steps {
                script {
					def nexusUrl = httpRequest(url: backEndBaseUrl + 'nexus/url/' + environmentGroup +'/dev', httpMode: 'GET', contentType: 'TEXT_PLAIN')
					def mvnCmd = mvnBin + ' ' + mvnOpts + ' deploy -DaltDeploymentRepository=' + nexusUrl.content
					try {
						sh mvnCmd
					} catch (e1) {
						try {
							echo 'DEV Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								sh mvnCmd
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "DEV Release - ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
                }
            }
        }
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* UAT Release Flow */
		stage('UAT Release Approval') {
			steps {
				echo 'Confirm UAT Release'
				script {
					def stepApproval = input(message: 'Approve Release to UAT?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
					echo stepApproval + " approved UAT release."
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated UAT Release.")
				}
	
			}
		}
		stage('UAT Release Process') {
			agent {
					node {
					label 'master'
					customWorkspace pipelineWorkspace
				}
			}
			steps {
				script {
					def nexusUrl = httpRequest(url: backEndBaseUrl + 'nexus/url/' + environmentGroup +'/uat', httpMode: 'GET', contentType: 'TEXT_PLAIN')
					def mvnCmd = mvnBin + ' ' + mvnOpts + ' deploy -DaltDeploymentRepository=' + nexusUrl.content
					try {
						sh mvnCmd
					} catch (e1) {
						try {
							echo 'UAT Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								sh mvnCmd
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
				}
			}
		}
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* STAGE Release Flow */
		stage('STAGE Release Approval') {
			steps {
				echo 'Confirm STAGE Release'
				script {
					def stepApproval = input(message: 'Approve Release to STAGE?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
					echo stepApproval + " approved STAGE release."
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated STAGE Release.")
				}
	
			}
		}
		stage('STAGE Release Process') {
			agent {
					node {
					label 'master'
					customWorkspace pipelineWorkspace
				}
			}
			steps {
				script {
					def nexusUrl = httpRequest(url: backEndBaseUrl + 'nexus/url/' + environmentGroup +'/stage', httpMode: 'GET', contentType: 'TEXT_PLAIN')
					def mvnCmd = mvnBin + ' ' + mvnOpts + ' deploy -DaltDeploymentRepository=' + nexusUrl.content
					try {
						sh mvnCmd
					} catch (e1) {
						try {
							echo 'STAGE Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								sh mvnCmd
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
				}
			}
		}
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- */
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */
		/* PRODUCTION Release Flow */
		stage('PRODUCTION Release Approval') {
			steps {
				echo 'Confirm PRODUCTION Release'
				script {
                    def prodApprovalMap = input(message: 'Approve Release to PROD?', ok: 'Yes', submitter: approverGroup, submitterParameter: 'approver' , parameters: [text(name:'label', defaultValue: prodReleaseMsg, description: 'Attention - Please READ!'), text(name:'releaseTicketId', defaultValue:'MCR######', description: 'Production Release Ticket #:')] )
                    echo "${prodApprovalMap['approver']} approved PROD release using ticket ${prodApprovalMap['releaseTicketId']}" 
                    httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod/' + prodApprovalMap['approver'], httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: prodApprovalMap['releaseTicketId'] )
				}
	
			}
		}
		stage('PRODUCTION Release Process') {
			agent {
					node {
					label 'master'
					customWorkspace pipelineWorkspace
				}
			}
			steps {
				script {
					def nexusUrl = httpRequest(url: backEndBaseUrl + 'nexus/url/' + environmentGroup +'/prod', httpMode: 'GET', contentType: 'TEXT_PLAIN')
					def mvnCmd = mvnBin + ' ' + mvnOpts + ' deploy -DaltDeploymentRepository=' + nexusUrl.content
					try {
						sh mvnCmd
					} catch (e1) {
						try {
							echo 'PRODUCTION Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								sh mvnCmd
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
				}
			}
		}
		/* ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ */

    }
}
