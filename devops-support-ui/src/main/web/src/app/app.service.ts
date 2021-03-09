import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Cookie } from 'ng2-cookies';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpModule, Response } from '@angular/http'
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../environments/environment';
import * as jwt_decode from "jwt-decode";
import { CanActivate } from '@angular/router';
import { SearchBuildParams } from './components/audit/audit.component';
import { url } from 'inspector';

@Injectable()
export class AppService {
    private authenticated: boolean = false;
    private httpBasicAuthHeader: HttpHeaders;
    private currentUser: string;
    private currentUserRoles: Object[];

    //private baseUrl: string = 'http://localhost:8080/DevOpsSupport/secured/';
    private baseUrl: string = environment.backend_base_url + 'secured/';
    public baseUserUrl: string = 'user';

    constructor(private _http: HttpClient) {
        console.log('AppService - Constructor()');
    }

    authenticate(credentials, callback, errorCallback) : void {
        console.log("credentials: " + JSON.stringify(credentials));
        this.httpBasicAuthHeader = new HttpHeaders(credentials ? {
            authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
        } : {});

        //console.log("httpBasicAuthHeader: " + JSON.stringify(this.httpBasicAuthHeader));

        this._http.get(this.baseUrl + this.baseUserUrl, { headers: this.httpBasicAuthHeader }).subscribe(response => {
            console.log('Response: ' + JSON.stringify(response));
            console.log('Login Response: ' + response['username'] + ' / ' + response['authenticated']);
            if (response['username']) {
                this.authenticated = true;
                this.currentUser = response['username'];
                this.currentUserRoles = response['authorities'];
            } else {
                this.authenticated = false;
            }
            return callback && callback();
        }, error => { console.log('Error logging in...'); errorCallback && errorCallback() });

    }

    getAuditBaseUrl() : string {
        return environment.backend_base_url + 'jenkins/audit/PDF/';
    }

    logout() : void {
        this.authenticated = false;
        this.currentUser = undefined;
        this.currentUserRoles = [];
    }

    isUserLoggedIn() : boolean {
        return this.authenticated;
    }

    getUserRoles() : any {
        return this.currentUserRoles;
    }

    getRepositories(): Observable<HttpResponse<VCSRepository[]>> {
        return this._http.get<VCSRepository[]>(this.baseUrl + "repository", { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    getRepoTypes(): Observable<HttpResponse<VCSRepositoryType[]>> {
        return this._http.get<VCSRepositoryType[]>(this.baseUrl + "repository/type", { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    getEnvironment(): Observable<HttpResponse<Environment[]>> {
        return this._http.get<Environment[]>(this.baseUrl + "environment", { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    getBuildDetails(jobId: string, buildNumber: number): Observable<HttpResponse<BuildDetails>> {
        return this._http.get<BuildDetails>(this.baseUrl + "audit/" + jobId + "/" + buildNumber, { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    getAllBuilds(): Observable<HttpResponse<BuildDetails[]>> {
        return this._http.get<BuildDetails[]>(this.baseUrl + "builds", { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    getBuildEntries(searchParams: SearchBuildParams): Observable<HttpResponse<BuildDetails[]>> {
        return this._http.post<BuildDetails[]>(this.baseUrl + "builds/search", searchParams, { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    saveRepo(repo: VCSRepository): Observable<VCSRepository> {
        var url: string = this.baseUrl + "repository";
        return this._http.post<VCSRepository>(url, repo, { headers: this.httpBasicAuthHeader });
    }

    saveEnv(env: Environment): Observable<Environment> {
        var url: string = this.baseUrl + "environment";
        return this._http.post<Environment>(url, env, { headers: this.httpBasicAuthHeader });
    }

    getUserPermissions(username: string): Observable<HttpResponse<UserPermission>> {
        return this._http.get<UserPermission>(this.baseUrl + "permission/" + username, { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    removeUserPermission(username: string, repoId: number): Observable<HttpResponse<UserPermission>> {
        return this._http.post<UserPermission>(this.baseUrl + "permission/remove/" + username + "/" + repoId, '', { headers: this.httpBasicAuthHeader, observe: 'response' });
    }

    addUserPermission(username: string, repoId: number): Observable<HttpResponse<UserPermission>> {
        return this._http.post<UserPermission>(this.baseUrl + "permission/add/" + username + "/" + repoId, '', { headers: this.httpBasicAuthHeader, observe: 'response' });
    }
}

export interface VCSRepositoryType {
    id: number;
    name: string;
    description: string;
}

export interface VCSRepository {
    id: number;
    url: string;
    description: string;
    extAlias: string;
    fsLocation: string;
    tagBase: string;
    groupEmailAddress: string,
    environmentGroup: string,
    disabled: boolean;
}

export interface BuildDetails {
    jobId: string;
    buildNumber: number;
    description: string;
    modifiedBy: string;
    modifiedOn: Date;
    modifiedByServer: string;
    repository: VCSRepository;
    auditEntries: AuditEntry[];
    fsLocation: string;
    tagName: string;
}

export interface AuditEntry {
    id: number;
    status: string;
    environment: string;
    approver: string;
    time: Date;
    releaseTicketId: string;
    exceptionMsg: string;
    modifiedOn: Date;
    modifiedBy: string;
    modifiedByHost: string;
}

export interface Environment {
    environmentGroup: string,
    environmentId: string,
    description: string,
    baseLocation: string,
    nexusUploadUrl: string,
    nexusBrowseUrl: string
}

export interface UserPermission {
    username: string,
    fullName: string,
    disabled: boolean,
    repositories: VCSRepository[]
}