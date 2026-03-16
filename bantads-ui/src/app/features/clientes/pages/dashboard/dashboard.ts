import { Component } from '@angular/core';
import { SearchInput } from '../../../../shared/components/search-input/search-input';
import { ButtonNotification } from '../../../../shared/components/button-notification/button-notification';
import { CreditCard } from '../../components/credit-card/credit-card';
import { ExtractFeed } from '../../components/extract-feed/extract-feed';
import { UltimasMovimentacoes } from '../../components/ultimas-movimentacoes/ultimas-movimentacoes';

@Component({
  selector: 'app-dashboard',
  imports: [SearchInput, ButtonNotification, CreditCard, ExtractFeed, UltimasMovimentacoes],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {}
