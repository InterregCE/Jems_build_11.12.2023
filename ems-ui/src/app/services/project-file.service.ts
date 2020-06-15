import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { ProjectFileStorageService, PageOutputProjectFile, InputProjectFileDescription, OutputProjectFile } from '@cat/api';

@Injectable()
export class ProjectFileService {

  basePath = '/api/project';

  constructor(private http: HttpClient,
              private service: ProjectFileStorageService) { }

  public getProjectFiles(project: number, size: number): Observable<PageOutputProjectFile> {
    return this.http.get<PageOutputProjectFile>(`${this.basePath}/${project}/file?size=${size}&sort=updated,desc`);
  }

  public addProjectFile(project: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.basePath}/${project}/file/`, formData);
  }

  public getDownloadLink(projectId: number, fileId: number): string {
    return `${this.basePath}/${projectId}/file/${fileId}`;
  }

  public deleteFile(projectId: number, fileId: number): Observable<any> {
    return this.service.deleteFile(fileId, projectId);
  }

  public setDescriptionToFile(projectId: number,
                              fileId: number,
                              description: InputProjectFileDescription
  ): Observable<OutputProjectFile> {
    return this.service.setDescriptionToFile(fileId, projectId, description);
  }

  // TODO done with download story
  // public getProjectFile(project: number, filename: string): Observable<ByteArrayResource> {
  //  return this.service.downloadFile(filename, project);
  // }

}
