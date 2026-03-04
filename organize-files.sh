#!/bin/bash

echo "========================================"
echo "  Organizing Project Files"
echo "========================================"
echo ""

# Create .kiro/docs folder if it doesn't exist
mkdir -p .kiro/docs

echo "Moving documentation files to .kiro/docs..."
echo ""

# Move all MD files except essential ones
for file in *.md; do
    if [ -f "$file" ] && \
       [ "$file" != "README.md" ] && \
       [ "$file" != "NETLIFY_DEPLOYMENT.md" ] && \
       [ "$file" != "RENDER_DEPLOYMENT.md" ] && \
       [ "$file" != "START_HERE.md" ]; then
        echo "Moving $file..."
        mv "$file" .kiro/docs/ 2>/dev/null
    fi
done

# Move text files
echo ""
echo "Moving text files..."
for file in *.txt; do
    if [ -f "$file" ]; then
        echo "Moving $file..."
        mv "$file" .kiro/docs/ 2>/dev/null
    fi
done

# Move shell scripts (except essential ones)
echo ""
echo "Moving shell scripts..."
for file in *.sh; do
    if [ -f "$file" ] && \
       [ "$file" != "start.sh" ] && \
       [ "$file" != "rebuild.sh" ] && \
       [ "$file" != "organize-files.sh" ]; then
        echo "Moving $file..."
        mv "$file" .kiro/docs/ 2>/dev/null
    fi
done

# Move batch scripts (except essential ones)
echo ""
echo "Moving batch scripts..."
for file in *.bat; do
    if [ -f "$file" ] && \
       [ "$file" != "start.bat" ] && \
       [ "$file" != "rebuild.bat" ] && \
       [ "$file" != "organize-files.bat" ]; then
        echo "Moving $file..."
        mv "$file" .kiro/docs/ 2>/dev/null
    fi
done

# Move docker-compose production file
echo ""
echo "Moving docker-compose.prod.yml..."
if [ -f "docker-compose.prod.yml" ]; then
    mv docker-compose.prod.yml .kiro/docs/ 2>/dev/null
fi

# Move Makefile
echo "Moving Makefile..."
if [ -f "Makefile" ]; then
    mv Makefile .kiro/docs/ 2>/dev/null
fi

# Replace README.md with README_NEW.md
echo ""
echo "Updating README.md..."
if [ -f "README_NEW.md" ]; then
    if [ -f "README.md" ]; then
        echo "Backing up old README.md..."
        mv README.md .kiro/docs/README_OLD.md 2>/dev/null
    fi
    echo "Using new README.md..."
    mv README_NEW.md README.md 2>/dev/null
fi

echo ""
echo "========================================"
echo "  Organization Complete!"
echo "========================================"
echo ""
echo "Files moved to: .kiro/docs/"
echo ""
echo "Kept in root:"
echo "  - README.md (updated)"
echo "  - START_HERE.md"
echo "  - NETLIFY_DEPLOYMENT.md"
echo "  - RENDER_DEPLOYMENT.md"
echo "  - start.sh"
echo "  - rebuild.sh"
echo "  - docker-compose.yml"
echo "  - .env"
echo "  - .env.example"
echo "  - .env.production.example"
echo "  - .gitignore"
echo ""
echo "Next steps:"
echo "  1. Review the changes"
echo "  2. Run: git add ."
echo "  3. Run: git commit -m 'Organize project files'"
echo "  4. Run: git push origin main"
echo "  5. Follow START_HERE.md for deployment"
echo ""
