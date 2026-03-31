export class Cliente {
    constructor(
        public id: number = 0,
        public nome: string = '',
        public cpf: number = 0,
        public email: string ='',
        public senha: string = '', 
        public salario: number = 0,) {
    } 
}
