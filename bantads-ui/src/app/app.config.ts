import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { BsModalService } from 'ngx-bootstrap/modal';

export const appConfig: ApplicationConfig = {
  providers: [
    BsModalService,
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes)
  ]
};
