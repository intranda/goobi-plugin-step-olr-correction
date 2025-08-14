
			document.addEventListener("DOMContentLoaded", function() {
				initImage();
			});
			console.log("CORRECT VERSION")

			function restoreScrollAndFocus(scrolldown) {
				if(window.savedOLR) {
					if(window.savedOLR.entryId) {
						let entry = document.getElementById(window.savedOLR.entryId);
						console.log(entry);
						$(entry).find("input").first().focus();
					}
					console.log("savedOlr:",window.savedOLR)
					if(scrolldown) {
						document.getElementById("qaform:entrylist").scrollTop = 999999999;
					} else {
						document.getElementById("qaform:entrylist").scrollTop = window.savedOLR.scrollTop;
					}
				}
			}

			function saveScrollAndFocus(btn) {
				let add = parseInt(btn.dataset.entryid)
				if(btn.id.indexOf("moveButton") >= 0) {
					add = add * 50;
				} else if(btn.id.indexOf("pasteButton") >= 0 || btn.id.indexOf("abortButton") >= 0) {
					add = add * -50;
				} else {
					add = 0;
				}
				let delId = "tocEntry" + btn.dataset.entryid;
				let entry = document.querySelector(".active-entry");
				let entryId = entry?.id;
				if(entry) {
					if(entryId > delId) {
						let num = parseInt(entryId.replace("tocEntry", ""))
						entryId = "tocEntry" + (num-1)
					}
				}
				let entryList = document.getElementById("qaform:entrylist");
				let scrollTop = entryList?.scrollTop;
				if(entryList){
					window.savedOLR = {entryId: entryId, scrollTop: scrollTop+add}
				}
			}

		faces.ajax.addOnEvent(function(data) {
			let ajaxstatus = data.status; // Can be "begin", "complete" and "success"
			let id = data.source.id;
			let restore = id.indexOf("deleteButton")>=0
							|| id.indexOf("pasteButton")>=0
							|| id.indexOf("abortButton")>=0
							|| id.indexOf("moveButton")>=0
							|| id.indexOf("addEntryButton") >= 0;
			let scrollDown = id.indexOf("addEntryButton") >= 0;
			console.log(id, restore)
			switch (ajaxstatus) {
				case "begin":
					if(restore) {
						console.log("would now save stuff");
						saveScrollAndFocus(data.source);
					}
				case "success": // This is called when ajax response is successfully processed.
					if(restore) {
						console.log("restoring...")
						setTimeout(() => {
							restoreScrollAndFocus(scrollDown);
						}, 500);
					}
					initImage();
					break;
			}

		});

		function copyValue(element, e) {
			console.log(element.id);
			if (element.id =='qaform:first:txtMoveTo2') {
				document.getElementById('qaform:second:txtMoveTo2').value = element.value;
			} else if (element.id =='qaform:second:txtMoveTo2'){
				document.getElementById('qaform:first:txtMoveTo2').value = element.value;
			} else if (element.id =='qaform:first_image:txtImageMoveTo2') {
				document.getElementById('qaform:second_image:txtImageMoveTo2').value = element.value;
			} else {
				document.getElementById('qaform:first_image:txtImageMoveTo2').value = element.value;
			}

			let keycode;
			if (window.event)
				keycode = window.event.keyCode;
			else if (e)
				keycode = e.which;
			else
				return true;

			if (keycode == 13) {
				document.getElementById(element.id).nextSibling.click();
				return false;
			} else
				return true;

		}

		const getTilesource = () => {
			const element = document.querySelector('[id$="tileSource"]');
			if (element) {
				let tileSource = element.value;
				return tileSource ? tileSource : '';
			}
			return '';
		};

        const getProcessId = () => {
            const element = document.querySelector('[id$="processId"]');
            if (element) {
                let processId = element.value;
                return processId ? processId : '';
            }
            return '';
        };

        const getScaleFactor = () => {
            const element = document.querySelector('[id$="scaleFactor"]');
            if (element) {
                let scaleFactor = element.value;
                return scaleFactor ? parseFloat(scaleFactor) : 1.0;
            }
            return 1.0;
        };

        const getShowAllEntries = () => {
            const element = document.querySelector('[id$="showAllEntries"]');
            if (element) {
                let showAllEntries = element.value;
                return showAllEntries === 'on';
            }
            return false;
        };

		// Global variable to hold the current image view instance
		let currentViewImage = null;

		const destroyImageView = () => {
			if (currentViewImage) {
				try {
					// Destroy the current image view instance
					if (currentViewImage.viewer) {
						currentViewImage.viewer.destroy();
					}
					if (currentViewImage.close) {
						currentViewImage.close();
					}
					// Remove any event listeners
					$('#mainImage').off('click');
				} catch (error) {
					console.warn('Error destroying image view:', error);
				}
				currentViewImage = null;
			}
		};

		const initImage = () => {
			// Destroy existing image view before creating a new one
			destroyImageView();

			let entryGroup = {
					name : "entry",
					styleClass : "entry",
					interactive: false
				};
			let authorGroup = {
					name : "author",
					styleClass : "author",
					interactive: false
				};
			let pagenumGroup = {
					name : "pagenum",
					styleClass : "pagenum",
					interactive: false
				};
			let institutionGroup = {
					name : "institution",
					styleClass : "institution",
					interactive: false
				};
			let titleGroup = {
					name : "title",
					styleClass : "title",
					interactive: false
				};

			let entryBorderGroup = {
					name : "entry-border",
					styleClass: "entry-border",
					interactive: false
				};
			let authorBorderGroup = {
					name : "author-border",
					styleClass: "author-border",
					interactive: false
				};
			let pagenumBorderGroup = {
					name : "pagenum-border",
					styleClass: "pagenum-border",
					interactive: false
				};
			let institutionBorderGroup = {
					name : "institution-border",
					styleClass: "institution-border",
					interactive: false
				};
			let titleBorderGroup = {
					name : "title-border",
					styleClass: "title-border",
					interactive: false
				};
			let imageViewConfig = {
					global: {
						zoomSpeed: 1.2,
						divId : 'mainImage',
						zoomSlider : 'zoomSlider',
						maxZoomLevel: 10,
						persistZoom: true,
						persistRotation: true,
						persistenceId: getProcessId(),
						overlayGroups: [entryGroup, authorGroup, pagenumGroup, institutionGroup, titleGroup,
							entryBorderGroup, authorBorderGroup, pagenumBorderGroup, institutionBorderGroup, titleBorderGroup]
					},
					image: {
						mimeType: "image/jpg",
						tileSource : getTilesource(),
					}
				};
			currentViewImage = new ImageView.Image(imageViewConfig);
			currentViewImage.close();
			currentViewImage.load().then(function() {
				console.log(imageViewConfig.image.tileSource);
				const entriesElement = document.querySelector('[id$="entriesJson"]');
				entries = JSON.parse(entriesElement.value);
				scaleFactor = getScaleFactor();
				if(getShowAllEntries()) {
					drawAllRects(currentViewImage, entries, scaleFactor);
				} else {
				// $('#tocEntry'+0).find('input').first().focus();
				}
				$( '#mainImage' ).click( function( event ) {
					if(event.target.nodeName !== "CANVAS" || event.ctrlKey) {
						return;
					}
					let pixel = new OpenSeadragon.Point( event.offsetX, event.offsetY );
					let pos = currentViewImage.viewer.viewport.viewerElementToImageCoordinates( pixel );
					findAndMarkRect(entries, pos, scaleFactor);
				} );
			}).catch(function(err) {
				console.log(err);
			});
			function drawAllRects(viewImage, entries, scaleFactor) {
				unDraw(viewImage);
				entries.forEach((entry) => {
					entry.boxes.forEach((box) => {
						var add = 1;
						if(box.type === "entry") {
							add = 3;
						}
						var rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
						viewImage.overlays.drawRect(rect, box.type);
						rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
						viewImage.overlays.drawRect(rect, box.type + "-border");
					});
				})
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

            const initRectDraw = () => {
                const elements = document.querySelectorAll('[data-plugin-drawRect');
                elements.forEach((element) => {
                    element.addEventListener('click', () => {
                        const target = element.dataset.pluginDrawRect;
                        switch (target) {
                            case "selected":
                                drawCurrentSelected();
                                break;
                            case "all":
                                drawAllRects(currentViewImage, entries, getScaleFactor());
                                break;
                            default:
                                drawRectNumber(currentViewImage, entries, getScaleFactor(), target)
                        }
                    });
                });
            }
			// this is placed here for simplicity, as initImage is called on load/ajax
			initRectDraw();

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
				drawRectNumber(currentViewImage, entries, scaleFactor, activeEntryId);
			}

			function findAndMarkRect(entries, pos, scaleFactor) {
				entries.forEach((entry) => {
					entry.boxes.forEach((box) => {
						if(box.type != "entry") {
							return;
						}
						var rect = new OpenSeadragon.Rect((box.x/scaleFactor)-1, (box.y/scaleFactor)-1, (box.width/scaleFactor)+2, (box.height/scaleFactor)+2);
						if(pos.x>rect.x && pos.x<rect.x+rect.width && pos.y>rect.y && pos.y<rect.y+rect.height) {
							var $inputs = $('#tocEntry'+i).find('input');
							$inputs.last().focus();
							$inputs.first().focus();
						}
					})
				})
			}
		};