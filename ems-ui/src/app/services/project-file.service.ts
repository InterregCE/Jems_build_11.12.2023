import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { ProjectFileStorageService, PageOutputProjectFile } from '@cat/api';

@Injectable()
export class ProjectFileService {

  basePath = '/api/project';

  constructor(private http: HttpClient,
              private service: ProjectFileStorageService) { }

  public getProjectFiles(project: number, size: number): Observable<PageOutputProjectFile> {
    return this.http.get<PageOutputProjectFile>(`${this.basePath}/${project}/file/list?size=${size}&sort=updated,desc`);
  }

  public addProjectFile(project: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.basePath}/${project}/file/`, formData);
  }

  // TODO done with download story
  // public getProjectFile(project: number, filename: string): Observable<ByteArrayResource> {
  //  return this.service.downloadFile(filename, project);
  // }

}