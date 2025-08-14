document.addEventListener("DOMContentLoaded", function() {
	initImage();
});

function restoreScrollAndFocus(scrolldown) {
	if(window.savedOLR) {
		if(window.savedOLR.entryId) {
			const entry = document.getElementById(window.savedOLR.entryId);
			const firstInput = entry?.querySelector('input');
			if (firstInput) {
				firstInput.focus();
			}
		}
		const entryList = document.getElementById("qaform:entrylist");
		if (entryList) {
			entryList.scrollTop = scrolldown ? 999999999 : window.savedOLR.scrollTop;
		}
	}
}

function saveScrollAndFocus(btn) {
	let add = parseInt(btn.dataset.entryid);
	if(btn.id.includes("moveButton")) {
		add = add * 50;
	} else if(btn.id.includes("pasteButton") || btn.id.includes("abortButton")) {
		add = add * -50;
	} else {
		add = 0;
	}
	const delId = `tocEntry${btn.dataset.entryid}`;
	const entry = document.querySelector(".active-entry");
	let entryId = entry?.id;
	if(entry && entryId > delId) {
		const num = parseInt(entryId.replace("tocEntry", ""));
		entryId = `tocEntry${num-1}`;
	}
	const entryList = document.getElementById("qaform:entrylist");
	const scrollTop = entryList?.scrollTop;
	if(entryList){
		window.savedOLR = {entryId: entryId, scrollTop: scrollTop+add};
	}
}

faces.ajax.addOnEvent(function(data) {
	const ajaxstatus = data.status;
	const id = data.source.id;

	// Only handle specific button actions that actually modify the entry list structure
	const isStructuralChange = ["deleteButton", "pasteButton", "abortButton", "moveButton", "addEntryButton"]
		.some(action => id.includes(action));

	// Also handle navigation events (pagination) - these need image reinitialization too
	const isNavigation = data.source.closest && (
		data.source.closest('.dataTables__paginator') !== null ||
		["showOcrButton", "showAllButton"].some(action => id.includes(action))
	);

	// Ignore PICA preview and form input AJAX events
	const isPicaPreview = id.includes("showPicaPreview");
	const isFormInput = ["title", "authors", "pageLabel"].some(field => id.includes(field));

	// Only proceed if this is a structural change or navigation
	if ((!isStructuralChange && !isNavigation) || isPicaPreview || isFormInput) {
		return;
	}

	const scrollDown = id.includes("addEntryButton");

	switch (ajaxstatus) {
		case "begin":
			destroyImageView();
			if (isStructuralChange) {
				saveScrollAndFocus(data.source);
			}
			break;
		case "success":
			if (isStructuralChange) {
				setTimeout(() => {
					restoreScrollAndFocus(scrollDown);
				}, 500);
			}
			initImage();
			break;
	}

});

const copyValue = (element, e) => {
	const copyTargets = {
		'qaform:first:txtMoveTo2': 'qaform:second:txtMoveTo2',
		'qaform:second:txtMoveTo2': 'qaform:first:txtMoveTo2',
		'qaform:first_image:txtImageMoveTo2': 'qaform:second_image:txtImageMoveTo2',
		'qaform:second_image:txtImageMoveTo2': 'qaform:first_image:txtImageMoveTo2'
	};

	const targetId = copyTargets[element.id];
	if (targetId) {
		const targetElement = document.getElementById(targetId);
		if (targetElement) {
			targetElement.value = element.value;
		}
	}

	const keycode = window.event?.keyCode || e?.which;
	if (keycode === 13) {
		element.nextSibling?.click();
		return false;
	}
	return true;
};

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

const highlightTocEntry = (element) => {
	document.querySelectorAll('.toc-entry').forEach(entry => {
		entry.classList.remove('active-entry');
	});

	// Add active-entry class to the closest toc-entry parent
	const tocEntry = element.closest('.toc-entry');
	if (tocEntry) {
		tocEntry.classList.add('active-entry');
	}
};

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
			document.getElementById('mainImage')?.removeEventListener?.('click', this.clickHandler);
		} catch (error) {
			console.warn('Error destroying image view:', error);
		}
		currentViewImage = null;
	}
};

const initImage = () => {

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
		const entriesElement = document.querySelector('[id$="entriesJson"]');
		entries = JSON.parse(entriesElement.value);
		scaleFactor = getScaleFactor();

		// Wait for overlays to be available before drawing rectangles
		const waitForOverlays = () => {
			if (currentViewImage.overlays) {
				if(getShowAllEntries()) {
					drawAllRects(currentViewImage, entries, scaleFactor);
				}
			} else {
				setTimeout(waitForOverlays, 100);
			}
		};

		waitForOverlays();

		const mainImage = document.getElementById('mainImage');
		if (mainImage) {
			mainImage.addEventListener('click', function(event) {
				if(event.target.nodeName !== "CANVAS" || event.ctrlKey) {
					return;
				}
				const pixel = new OpenSeadragon.Point(event.offsetX, event.offsetY);
				const pos = currentViewImage.viewer.viewport.viewerElementToImageCoordinates(pixel);
				findAndMarkRect(entries, pos, scaleFactor);
			});
		}
	}).catch(function(err) {
		console.log(err);
	});

	function drawAllRects(viewImage, entries, scaleFactor) {
		if (!viewImage.overlays) {
			console.error("viewImage.overlays is not available. Cannot draw rectangles.");
			return;
		}

		unDraw(viewImage);
		entries.forEach((entry, entryIndex) => {
			entry.boxes.forEach((box, boxIndex) => {
				let add = 1;
				if(box.type === "entry") {
					add = 3;
				}
				let rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
				viewImage.overlays.drawRect(rect, box.type);
				rect = new OpenSeadragon.Rect((box.x/scaleFactor)-add, (box.y/scaleFactor)-add, (box.width/scaleFactor)+add*2, (box.height/scaleFactor)+add*2);
				viewImage.overlays.drawRect(rect, box.type + "-border");
			});
		})
	}

	function unDraw(viewImage) {
		if (!viewImage.overlays) {
			console.warn("viewImage.overlays is not available. Cannot undraw rectangles.");
			return;
		}

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

	// Simple rectangle draw initialization - called each time initImage runs
	const initRectDraw = () => {
		// Use event delegation for better compatibility with AJAX-generated content
		document.removeEventListener('focus', window._globalRectDrawHandler, true);
		document.removeEventListener('click', window._globalRectClickHandler, true);

		// Global focus handler for rectangle drawing only
		window._globalRectDrawHandler = (event) => {
			const target = event.target;
			if (target && target.dataset && target.dataset.pluginDrawRect) {
				if (!currentViewImage || !currentViewImage.overlays) {
					return;
				}

				const rectTarget = target.dataset.pluginDrawRect;
				setTimeout(() => {
					switch (rectTarget) {
						case "selected":
							drawCurrentSelected();
							break;
						case "all":
							drawAllRects(currentViewImage, entries, scaleFactor);
							break;
						default:
							drawRectNumber(currentViewImage, entries, scaleFactor, parseInt(rectTarget));
					}
				}, 50);
			}
		};

		// Global click handler for buttons
		window._globalRectClickHandler = (event) => {
			const target = event.target;
			if (target && target.dataset && target.dataset.pluginDrawRect &&
				(target.tagName === 'A' || target.tagName === 'BUTTON')) {

				event.preventDefault();

				if (!currentViewImage || !currentViewImage.overlays) {
					return;
				}

				const rectTarget = target.dataset.pluginDrawRect;
				switch (rectTarget) {
					case "selected":
						drawCurrentSelected();
						break;
					case "all":
						drawAllRects(currentViewImage, entries, scaleFactor);
						break;
					default:
						drawRectNumber(currentViewImage, entries, scaleFactor, parseInt(rectTarget));
				}
			}
		};

		// Add event listeners with capture=true to catch events early
		document.addEventListener('focus', window._globalRectDrawHandler, true);
		document.addEventListener('click', window._globalRectClickHandler, true);
	}

	// Initialize rectangle drawing
	initRectDraw();

	function drawRectNumber(viewImage, entries, scaleFactor, entryNumber) {
		if (!viewImage.overlays) {
			console.error("viewImage.overlays is not available. Cannot draw rectangles.");
			return;
		}

		unDraw(viewImage);
		for(let i = 0; i < entries.length; i++) {
			if(i !== entryNumber) {
				continue;
			}
			const entry = entries[i];
			for(let k = 0; k < entry.boxes.length; k++) {
				const box = entry.boxes[k];
				const add = box.type === "entry" ? 3 : 1;
				const rect = new OpenSeadragon.Rect(
					(box.x/scaleFactor) - add,
					(box.y/scaleFactor) - add,
					(box.width/scaleFactor) + (add*2),
					(box.height/scaleFactor) + (add*2)
				);
				viewImage.overlays.drawRect(rect, box.type);
				viewImage.overlays.drawRect(rect, `${box.type}-border`);
			}
		}
	}

	function drawCurrentSelected() {
		const activeEntry = document.querySelector('.active-entry');
		if(!activeEntry?.id) {
			const firstEntry = document.querySelector('#tocEntry0 input');
			firstEntry?.focus();
			return;
		}
		const activeEntryId = activeEntry.id.replace('tocEntry', '');
		drawRectNumber(currentViewImage, entries, scaleFactor, activeEntryId);
	}

	function findAndMarkRect(entries, pos, scaleFactor) {
		entries.forEach((entry, entryIndex) => {
			entry.boxes.forEach((box) => {
				if(box.type !== "entry") {
					return;
				}
				const rect = new OpenSeadragon.Rect(
					(box.x/scaleFactor) - 1,
					(box.y/scaleFactor) - 1,
					(box.width/scaleFactor) + 2,
					(box.height/scaleFactor) + 2
				);
				if(pos.x > rect.x && pos.x < rect.x + rect.width &&
					pos.y > rect.y && pos.y < rect.y + rect.height) {
					const entryElement = document.getElementById(`tocEntry${entryIndex}`);
					const inputs = entryElement?.querySelectorAll('input');
					if (inputs && inputs.length > 0) {
						inputs[inputs.length - 1].focus();
						inputs[0].focus();
					}
				}
			});
		});
	}
};