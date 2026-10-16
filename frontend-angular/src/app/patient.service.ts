import {computed, Injectable, input, signal, Signal} from "@angular/core";
import {httpResource, HttpResourceRef} from '@angular/common/http';
import {PatientModel} from './patient.model';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private getPatientsResources: HttpResourceRef<PatientModel[] | undefined> = httpResource(() => '/api/patients')

  getPatients(): Signal<PatientModel[]> {
    // return signal([
    //   {id: '1234-abcd', name: 'john doe', dateNaissance: '2014-11-27', typePompe: 'PATCH'},
    //   {id: '9876-zyxw', name: 'jane doe', dateNaissance: '2014-11-27', typePompe: 'PATCH'},
    // ]);
    return computed(() => this.getPatientsResources.value() ?? []);
  }

  findPatientById(patientId: Signal<string>) {
    let findPatientByIdResource: HttpResourceRef<PatientModel | undefined> = httpResource(() => `/api/patients/${patientId()}`)
    return computed(() => findPatientByIdResource.value());
  }

}
