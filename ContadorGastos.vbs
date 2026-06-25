' ============================================================
'  Contador de Gastos - LAUNCHER SIN CONSOLA
'  Alternativa C: Ejecuta la aplicación sin mostrar ventana
'  de símbolo del sistema (consola negra).
'  Uso: Doble clic para iniciar silenciosamente
' ============================================================

Dim objShell, objFSO
Dim strProjectDir, strJar, strJava, strCmd

Set objShell = CreateObject("WScript.Shell")
Set objFSO   = CreateObject("Scripting.FileSystemObject")

' --- Obtener ruta del directorio del script ---
strProjectDir = objFSO.GetParentFolderName(WScript.ScriptFullName)

' --- Rutas de Java y JAR ---
strJava = "C:\Program Files\Java\jdk-21.0.10\bin\java.exe"
strJar  = strProjectDir & "\target\Contador_De_Gastos-1.0-SNAPSHOT.jar"

' --- Verificar que java.exe existe ---
If Not objFSO.FileExists(strJava) Then
    ' Intentar con java del PATH
    strJava = "java"
End If

' --- Verificar que el JAR existe ---
If Not objFSO.FileExists(strJar) Then
    MsgBox "No se encontró la aplicación compilada." & vbCrLf & vbCrLf & _
           "Ruta esperada:" & vbCrLf & strJar & vbCrLf & vbCrLf & _
           "Solución: Ejecuta primero 'compilar-y-ejecutar.bat'", _
           vbCritical, "Contador de Gastos - Error"
    WScript.Quit(1)
End If

' --- Cambiar directorio de trabajo al del proyecto ---
objShell.CurrentDirectory = strProjectDir

' --- Construir comando ---
strCmd = """" & strJava & """ -jar """ & strJar & """"

' --- Ejecutar sin mostrar ventana (0 = oculto) ---
objShell.Run strCmd, 0, False

Set objShell = Nothing
Set objFSO   = Nothing
WScript.Quit(0)
