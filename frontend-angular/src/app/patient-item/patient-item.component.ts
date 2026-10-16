import {Component, input} from "@angular/core";
import {RouterLink} from "@angular/router";
import {PatientModel} from '../patient.model';

@Component({
    imports: [
        RouterLink
    ],
  selector: "app-patient-item",
  styleUrl: "./patient-item.component.css",
  templateUrl: "./patient-item.component.html",
})
export class PatientItemComponent {

  patient = input.required<PatientModel>();


}
