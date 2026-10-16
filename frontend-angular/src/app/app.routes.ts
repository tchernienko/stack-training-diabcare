import { Routes } from '@angular/router';
import {PatientsListComponent} from './patients-list/patients-list.component';
import {PatientDetailsComponent} from './patient-details/patient-details.component';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';

export const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: '/patients'},
  {path: 'patients', component: PatientsListComponent},
  {path: 'patients/new', component: PageNotFoundComponent},
  {path: 'patients/:id', component: PatientDetailsComponent},
  {path: '**', component: PageNotFoundComponent},

];
