import angular from 'angular';
import '@angular/material'; // Import Material Design library
import { MyChatComponent } from './chat/chat.component';
import { MessageService } from './services/message.service';

const app = angular.module('myChatApp', [
  '@angular/material',
  // ... other necessary modules
]);

app.component('myChat', MyChatComponent);
app.service('messageService', MessageService);

// Configure Material You theme (replace with your theme configuration)
app.config(($mdThemingProvider) => {
  $mdThemingProvider.definePalette('my-palette', {
    primary: { '500': '#3F51B5' }, // Example primary color
    accent: { '500': '#FFC107' }, // Example accent color
  });
  $mdThemingProvider.theme('default')
    .usePalette('my-palette', 'primary')
    .accentPalette('my-palette', 'accent');
});

