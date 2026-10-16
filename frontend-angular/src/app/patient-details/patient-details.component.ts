import {Component, Signal, signal} from "@angular/core";
import {PatientModel} from '../patient.model';
import {PatientService} from '../patient.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  imports: [
  ],
  selector: "app-patient-details",
  styleUrl: "./patient-details.component.css",
  templateUrl: "./patient-details.component.html",
})
export class PatientDetailsComponent {

  patientId = signal<string>('');
  patient: Signal<PatientModel | undefined>;

  constructor(private patientService: PatientService, private route: ActivatedRoute) {
    this.route.params.subscribe(
      params => {this.patientId.set(params['id'])}
    );
    this.getPatientById()

    console.log('calling getPatientById of patient-details.component, patientId', this.patientId);
    this.patient = this.patientService.findPatientById(this.patientId);
  }

  ngOnInit() {
  }

  getPatientById() {
  }



}
