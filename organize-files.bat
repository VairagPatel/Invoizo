@echo off
echo ========================================
echo   Organizing Project Files
echo ========================================
echo.

REM Create .kiro/docs folder if it doesn't exist
if not exist ".kiro\docs" mkdir ".kiro\docs"

echo Moving documentation files to .kiro\docs...
echo.

REM Move all MD files except essential ones
for %%f in (*.md) do (
    if /i not "%%f"=="README.md" (
        if /i not "%%f"=="NETLIFY_DEPLOYMENT.md" (
            if /i not "%%f"=="RENDER_DEPLOYMENT.md" (
                if /i not "%%f"=="START_HERE.md" (
                    echo Moving %%f...
                    move "%%f" ".kiro\docs\" >nul 2>&1
                )
            )
        )
    )
)

REM Move text files
echo.
echo Moving text files...
for %%f in (*.txt) do (
    echo Moving %%f...
    move "%%f" ".kiro\docs\" >nul 2>&1
)

REM Move shell scripts (except essential ones)
echo.
echo Moving shell scripts...
for %%f in (*.sh) do (
    if /i not "%%f"=="start.sh" (
        if /i not "%%f"=="rebuild.sh" (
            if /i not "%%f"=="organize-files.sh" (
                echo Moving %%f...
                move "%%f" ".kiro\docs\" >nul 2>&1
            )
        )
    )
)

REM Move batch scripts (except essential ones)
echo.
echo Moving batch scripts...
for %%f in (*.bat) do (
    if /i not "%%f"=="start.bat" (
        if /i not "%%f"=="rebuild.bat" (
            if /i not "%%f"=="organize-files.bat" (
                echo Moving %%f...
                move "%%f" ".kiro\docs\" >nul 2>&1
            )
        )
    )
)

REM Move docker-compose production file
echo.
echo Moving docker-compose.prod.yml...
if exist "docker-compose.prod.yml" move "docker-compose.prod.yml" ".kiro\docs\" >nul 2>&1

REM Move Makefile
echo Moving Makefile...
if exist "Makefile" move "Makefile" ".kiro\docs\" >nul 2>&1

REM Replace README.md with README_NEW.md
echo.
echo Updating README.md...
if exist "README_NEW.md" (
    if exist "README.md" (
        echo Backing up old README.md...
        move "README.md" ".kiro\docs\README_OLD.md" >nul 2>&1
    )
    echo Using new README.md...
    move "README_NEW.md" "README.md" >nul 2>&1
)

echo.
echo ========================================
echo   Organization Complete!
echo ========================================
echo.
echo Files moved to: .kiro\docs\
echo.
echo Kept in root:
echo   - README.md (updated)
echo   - START_HERE.md
echo   - NETLIFY_DEPLOYMENT.md
echo   - RENDER_DEPLOYMENT.md
echo   - start.bat
echo   - rebuild.bat
echo   - docker-compose.yml
echo   - .env
echo   - .env.example
echo   - .env.production.example
echo   - .gitignore
echo.
echo Next steps:
echo   1. Review the changes
echo   2. Run: git add .
echo   3. Run: git commit -m "Organize project files"
echo   4. Run: git push origin main
echo   5. Follow START_HERE.md for deployment
echo.
pause
