import { Component } from "@angular/core";
import {RouterLink, RouterLinkActive} from '@angular/router';

@Component({
  imports: [
    RouterLinkActive,
    RouterLink
  ],
  selector: "app-site-header",
  styleUrl: "./site-header.component.css",
  templateUrl: "./site-header.component.html",
})
export class SiteHeaderComponent {}
