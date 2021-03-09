import groovy.json.JsonSlurper

pipeline {
    agent none
    environment {
        uniquePipelineId = 'FILE_RELEASE_1'
		devApproval = null;
        repoId='';
        repoUrl='';
        repoTag='';
        backEndBaseUrl='http://localhost:8888/DevOpsSupport/jenkins/';
		buildWorkspace = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/workspace"
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
						vcsType = input(message: 'VCS Type', ok: 'Continue', submitter: 'developerGroup', parameters: [choice(choices: ["SVN","GIT"], description: 'Select VCS Repository Type', name: 'VCS_TYPE') ])
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
							objMap.put(obj.id, [ "$obj.id", "$obj.url", "$obj.description", "$obj.tagBase" ])
							repoOptions.add(obj.id + " || " + obj.description)
						}
						
						// To avoid issues serializing JSON object, these need to be set to null
                        json = null
						repoList = null

                        vcsSelection = input(message: 'Required Pipeline Parameters', ok: 'Proceed', submitter: developerGroup, submitterParameter: 'submitter', parameters: [choice(choices: repoOptions, description: 'Select VCS Repository', name: 'VCS_URL'), string(name:'VCS_TAG', defaultValue:'v0.2-alpha', description: 'Enter Tag/Branch:'), string(name:'RELEASE_COMMENTS', defaultValue:'JIRA-1234: Changes in App.', description: 'Enter Release Comments:') ])
                        def tmp = vcsSelection['VCS_URL'].replace(' || ','|')
                        repoId = tmp.substring(0, tmp.indexOf('|')) as int
                        /*repoUrl = tmp.substring(tmp.indexOf('|') + 1, tmp.length())*/
						repoUrl = objMap.get(repoId)[1]
                        repoTag = vcsSelection['VCS_TAG']
						repoTagBase = objMap.get(repoId)[3]

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
                    customWorkspace buildWorkspace
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
					
					def data = readFile('ReleaseFiles')
					println(data)
					httpRequest(url: backEndBaseUrl + 'register/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: data )
                }
            }
        }
		/*
        stage('VCS Checkout') {
            agent {
                    node {
                    label 'master'
                    customWorkspace buildWorkspace
                }
            }
            steps {
                script {
                    echo 'Checking out code from [' + repoUrl + '] / Tag: [' + repoTag + ']'
                }
                checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: repoUrl]], branches: [[name: 'refs/tags/' + repoTag]]], poll: false
				script {
					def data = readFile('ReleaseFiles')
					println(data)
                    httpRequest(url: backEndBaseUrl + 'register/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: data )
				}
            }
        }
        */

        stage('DEV Release & Deployment') {
            steps {
                echo 'Confirm DEV Release'
                script {
                    def stepApproval = input(message: 'Approve Release to DEV?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
                    echo stepApproval + " approved DEV release."
					/* Audit APPROVAL */
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated DEV Release.")
					
					try {
						/* FILE Release */                    
	                    httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev' , httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
					} catch (e1) {
						try {
							echo 'DEV Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								
								/* FILE Release */                    
			                    httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/dev', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
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
		
		stage('UAT Release & Deployment') {
			steps {
				echo 'Confirm UAT Release'
				script {
					def stepApproval = input(message: 'Approve Release to UAT?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
					echo stepApproval + " approved UAT release."
					/* Audit APPROVAL */
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated UAT Release.")
					
					try {
						/* FILE Release */
						httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat' , httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
					} catch (e1) {
						try {
							echo 'UAT Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								
								/* FILE Release */
								httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "UAT Release - ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
				}
			}
		}
		
		/*
        stage('UAT Release & Deployment') {
            steps {
                echo 'Confirm UAT Release'
                script {
                    def uatApproval = input(message: 'Approve Release to UAT?', ok: 'Yes', submitter: 'developerGroup', submitterParameter: 'submitter' )
                    echo uatApproval + " approved UAT release."
                    //httpRequest(url: 'http://192.168.1.44:8080/DevOpsSupport/log', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "{ 'build': '${env.BUILD_NUMBER}', 'approver': '${uatApproval}', 'env': 'UAT' }")                    
                    httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/uat/' + uatApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
                }
            }
        }
        */

		stage('STAGE Release & Deployment') {
			steps {
				echo 'Confirm STAGE Release'
				script {
					def stepApproval = input(message: 'Approve Release to STAGE?', ok: 'Yes', submitter: developerGroup, submitterParameter: 'submitter' )
					echo stepApproval + " approved STAGE release."
					/* Audit APPROVAL */
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage/' + stepApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "Automated STAGE Release.")
					
					try {
						/* FILE Release */
						httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage' , httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
					} catch (e1) {
						try {
							echo 'STAGE Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								
								/* FILE Release */
								httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "STAGE Release - ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
				}
			}
		}
		
		/*
        stage('STAGE Release & Deployment') {
            steps {
                echo 'Confirm STAGE Release'
                script {
                    def stageApproval = input(message: 'Approve Release to STAGE?', ok: 'Yes', submitter: 'developerGroup', submitterParameter: 'submitter' )
                    echo "${stageApproval} approved STAGE release." 
                    //httpRequest(url: 'http://192.168.1.44:8080/DevOpsSupport/log', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "{ 'build': '${env.BUILD_NUMBER}', 'approver': '${stageApproval}', 'env': 'STAGE' }")                    
                    httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/stage/' + stageApproval, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
                }
            }
        }
        */

		stage('PROD Release & Deployment') {
			steps {
				echo 'Confirm PROD Release'
				script {
                    def stepApproval = input(message: 'Approve Release to PROD?', ok: 'Yes', submitter: approverGroup, submitterParameter: 'approver' , parameters: [text(name:'label', defaultValue: prodReleaseMsg, description: 'Attention - Please READ!'), text(name:'releaseTicketId', defaultValue:'MCR######', description: 'Production Release Ticket #:')] )
                    echo "${stepApproval['approver']} approved PROD release using ticket ${stepApproval['releaseTicketId']}" 
					/* Audit APPROVAL */
                    httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod/' + stepApproval['approver'], httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: stepApproval['releaseTicketId'] )
					
					try {
						/* FILE Release */
						httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod' , httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
					} catch (e1) {
						try {
							echo 'PROD Deployment failed. ' + e1.getMessage()
							retry(3) {
								input "Try Again ?"
								
								/* FILE Release */
								httpRequest(url: backEndBaseUrl + 'filecopy/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: '{ "buildWorkspace": "' + buildWorkspace + '" }')
							}
						} catch (e2) {
							httpRequest(url: backEndBaseUrl + 'audit/exception/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "PROD Release - ERROR: " + e2.getMessage())
							throw e2
						}
					}
					httpRequest(url: backEndBaseUrl + 'audit/success/' + uniquePipelineId + '/' + env.BUILD_NUMBER + '/prod', httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
					
					httpRequest(url: backEndBaseUrl + 'audit/' + uniquePipelineId + '/' + env.BUILD_NUMBER, httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: "OK")
				}
			}
		}

    }
}
