import { Component, ChangeDetectionStrategy } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import {SiteHeaderComponent} from './site-header/site-header.component';

@Component({
  selector: "app-root",
  imports: [RouterOutlet, SiteHeaderComponent],
  templateUrl: "./app.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: "./app.component.css",
})
export class AppComponent {}
