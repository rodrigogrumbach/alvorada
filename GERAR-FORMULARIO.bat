@echo off
rem Gera o Formulario de Esclarecimentos e o Anexo (etapa 5 do alexandria\PROCESSO.md) e abre a pasta dos PDFs.
rem Apenas executa o comando existente: .\mvnw.cmd exec:java@formulario
rem Etapa manual e deliberada: rode so depois de conferir as fichas que vao entrar.
chcp 65001 >nul
cd /d "%~dp0"

echo.
echo Gerar o Formulario de Esclarecimentos a partir das fichas "sugerido" e "pendente".
echo Confira antes as fichas que vao entrar: so as perguntas necessarias.
echo.
choice /c SN /m "Gerar agora"
if errorlevel 2 goto fim

call mvnw.cmd -q exec:java@formulario
if errorlevel 1 (
    echo.
    echo [ERRO] O formulario nao foi gerado. Veja as mensagens acima.
    pause
    exit /b 1
)

start "" explorer "%~dp0alexandria\formularios"
echo.
echo Revise os PDFs antes de enviar. Para refazer, apague os arquivos do formulario gerado antes.

:fim
pause
