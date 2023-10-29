# AppletRunner

Aplicação JAVA para remover a dependência do IExplorer para executar Applets Java.
Foi feita especificamente para usar com a aplicação SONHOv2 e SCLINICO dos Serviços Partilhados do Ministério da Saúde (SPMS) mas pode ser usada para correr outras Applets.

Pode ser usado o bat abaixo para arrancar a aplicação colocando o JRE e a aplicação numa share.
Isto facilita o deploy e futuros updates mas basta correrer a aplicação com o parâmetro correcto (o URL da aplicação) para que o sistema funcione.

-----------------------------------------------------------------------
@echo off

if exist c:\jre1.8.0_121\ (

      set JAVA_HOME=c:\jre1.8.0_121\
      
) else if exist c:\other_path\jre1.8.0_121\ (

      set JAVA_HOME=c:\other_path\jre1.8.0_121\
      
) else (

      set JAVA_HOME=\\\\server\share$\jre1.8.0_121\
      
)

%JAVA_HOME%\bin\java -jar \\\\server\share$\path\AppletRunner-1.0-SNAPSHOT-jar-with-dependencies.jar https://servidor/app?config=app

--------------------------------------------------------------------------

 

Basta mudar o link para outras applets (SonhoV2, SClinico, etc).
Não é necessário instalar o Java (correr o setup) nas máquinas, basta estar numa pasta qualquer (JAVA_HOME no bat acima). Pode-se instalar o JRE numa máquina e depois copiar a pasta onde ficou instalado para uma share.
Se o JRE estiver no disco local fica mais rápido a abrir a primeira vez. No exemplo o bat procura o JRE em dois locais no disco local e se não encontrar usa o JRE na share.
Não é aconselhado colocar o JAR AppletRunner no disco local porque se for necessário distribuir uma nova versão ou alterar um dos ficheiros de configuração é mais complicado de fazer o deploy.

 

Existem 3 ficheiros de configuração (a aplicação procura esses ficheiros primeiro na directoria actual em que o programa estiver a ser executado e se não existir procura na directoria onde está o JAR que está a ser executado) mas não precisam de estar presentes para a aplicação funcionar.

 

O ficheiro hosts serve apenas para pré-popular a cache DNS da aplicação (não do Sistema Operativo). Desse modo a aplicação não necessita de fazer requests de DNS.

 

O ficheiro printers.yml serve para apresentar impressoras à aplicação que não estão instaladas no Sistema Operativo. 
Serve para imprimir directamente para uma impressora de rede sem a impressora estar instalada localmente.
Todas as impressoras aparecem a todos os utilizadores.

 

O ficheiro apps.yml serve para forçar a abertura de determinados links que o SClinico manda abrir num browser específico, por exemplo aplicação de análises no IExplorer e a aplicação de MCDTs no google chrome. 
Todos os outros links são abertos no browser por defeito do utilizador. Também permite pegar nos parâmetros de um comando (das integrações do SClinico) e passar esses parâmetros para um browser (se o parâmetro for um URL).
Para usar regular expressions com comandos é necessário mais um jar na pasta (o demo-1.0.jar), senão não é preciso estar presente.
Quem não tem problemas deste tipo não precisa de usar este ficheiro.

Se não forem necessarias estas funcionalidades basta não colocar o ficheiro na mesma pasta do AppletRunner.

 
