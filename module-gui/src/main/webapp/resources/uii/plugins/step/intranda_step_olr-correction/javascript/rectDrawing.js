function drawAllRects(viewImage, entries, scaleFactor) {
    unDraw(viewImage);
    for(var i=0; i<entries.length;i++) {
        var entry = entries[i];
        for(var k=0; k<entry.boxes.length; k++) {
            var box = entry.boxes[k];
            var add = 1;
            if(box.type === "entry") {
                add = 3;
            }
            var rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
            viewImage.overlays.drawRect(rect, box.type);
            rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
            viewImage.overlays.drawRect(rect, box.type + "-border");
        }
    }
}

function unDraw(viewImage) {
    viewImage.overlays.unDraw("pagenum");
    viewImage.overlays.unDraw("author");
    viewImage.overlays.unDraw("institution");
    viewImage.overlays.unDraw("title");
    viewImage.overlays.unDraw("entry");
    
    viewImage.overlays.unDraw("pagenum-border");
    viewImage.overlays.unDraw("author-border");
    viewImage.overlays.unDraw("institution-border");
    viewImage.overlays.unDraw("title-border");
    viewImage.overlays.unDraw("entry-border");
}

function drawRectNumber(viewImage, entries, scaleFactor, entryNumber) {
    unDraw(viewImage);
    for(var i=0; i<entries.length;i++) {
        if(i != entryNumber) {
            continue;
        }
        var entry = entries[i];
        for(var k=0; k<entry.boxes.length; k++) {
            var box = entry.boxes[k];
            var add = 1;
            if(box.type === "entry") {
                add = 3;
            }
            var rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+(add*2), (box.height/scaleFactor)+(add*2));
            viewImage.overlays.drawRect(rect, box.type);
            rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+(add*2), (box.height/scaleFactor)+(add*2));
            viewImage.overlays.drawRect(rect, box.type + "-border");
        }
    }
}

function drawCurrentSelected(beanValue) {
    var activeEntryId = $('.active-entry').attr('id');
    if(!activeEntryId) {
        $('#tocEntry'+0).find('input').first().focus();
        return;
    }
    activeEntryId = activeEntryId.replace('tocEntry', '');
    drawRectNumber(viewImage, entries, scaleFactor, activeEntryId);
}

function findAndMarkRect(entries, pos, scaleFactor) {
    for(var i=0; i<entries.length;i++) {
        var entry = entries[i];
        for(var k=0; k<entry.boxes.length; k++) {
            var box = entry.boxes[k];                                               
            if(box.type != "entry") {
                continue;
            }
            var rect = new OpenSeadragon.Rect((box.x/scaleFactor)-1, (box.y/scaleFactor)-1, (box.width/scaleFactor)+2, (box.height/scaleFactor)+2);
            if(pos.x>rect.x && pos.x<rect.x+rect.width && pos.y>rect.y && pos.y<rect.y+rect.height) {
                var $inputs = $('#tocEntry'+i).find('input');
                $inputs.last().focus();
                $inputs.first().focus();
            }
        }
    }
}