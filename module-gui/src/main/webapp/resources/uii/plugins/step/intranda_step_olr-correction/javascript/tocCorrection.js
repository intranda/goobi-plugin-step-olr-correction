document.addEventListener("DOMContentLoaded", function() {
	initImage();
});

const restoreScrollAndFocus = (scrolldown) => {
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
};

const saveScrollAndFocus = (btn) => {
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
};

faces.ajax.addOnEvent((data) => {
	const ajaxstatus = data.status;
	const id = data.source.id;

	// Define action categories for better organization
	const structuralActions = ["deleteButton", "pasteButton", "abortButton", "moveButton", "addEntryButton"];
	const navigationActions = ["showOcrButton", "showAllButton"];
	const ignoredActions = ["showPicaPreview"];
	const formFields = ["title", "authors", "pageLabel"];

	const isStructuralChange = structuralActions.some(action => id.includes(action));
	const isNavigation = data.source.closest && (
		data.source.closest('.dataTables__paginator') !== null ||
		navigationActions.some(action => id.includes(action))
	);
	const isPicaPreview = ignoredActions.some(action => id.includes(action));
	const isFormInput = formFields.some(field => id.includes(field));

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

// Configuration and utility functions
const getTilesource = () => {
	const element = document.querySelector('[id$="tileSource"]');
	return element?.value || '';
};

const getProcessId = () => {
	const element = document.querySelector('[id$="processId"]');
	return element?.value || '';
};

const getScaleFactor = () => {
	const element = document.querySelector('[id$="scaleFactor"]');
	return element ? parseFloat(element.value) || 1.0 : 1.0;
};

const getShowAllEntries = () => {
	const element = document.querySelector('[id$="showAllEntries"]');
	return element?.value === 'on';
};

// Global variables
let currentViewImage = null;
let entries = null;
let scaleFactor = 1.0;

// Rectangle drawing functions
const drawAllRects = (viewImage, entries, scaleFactor) => {
	if (!viewImage.overlays) {
		console.error("viewImage.overlays is not available. Cannot draw rectangles.");
		return;
	}

	unDraw(viewImage);
	entries.forEach((entry, entryIndex) => {
		entry.boxes.forEach((box, boxIndex) => {
			const add = box.type === "entry" ? 3 : 1;
			const rect = new OpenSeadragon.Rect(
				(box.x/scaleFactor) - add,
				(box.y/scaleFactor) - add,
				(box.width/scaleFactor) + add*2,
				(box.height/scaleFactor) + add*2
			);
			viewImage.overlays.drawRect(rect, box.type);
			viewImage.overlays.drawRect(rect, `${box.type}-border`);
		});
	});
};

const unDraw = (viewImage) => {
	if (!viewImage.overlays) {
		console.warn("viewImage.overlays is not available. Cannot undraw rectangles.");
		return;
	}

	const overlayTypes = ["pagenum", "author", "institution", "title", "entry"];
	overlayTypes.forEach(type => {
		viewImage.overlays.unDraw(type);
		viewImage.overlays.unDraw(`${type}-border`);
	});
};

const drawRectNumber = (viewImage, entries, scaleFactor, entryNumber) => {
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
};

const drawCurrentSelected = () => {
	const activeEntry = document.querySelector('.active-entry');
	if(!activeEntry?.id) {
		const firstEntry = document.querySelector('#tocEntry0 input');
		firstEntry?.focus();
		return;
	}
	const activeEntryId = activeEntry.id.replace('tocEntry', '');
	drawRectNumber(currentViewImage, entries, scaleFactor, activeEntryId);
};

const findAndMarkRect = (entries, pos, scaleFactor) => {
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
};

const highlightTocEntry = (element) => {
	// Remove active state from all entries
	document.querySelectorAll('.toc-entry').forEach(entry => {
		entry.classList.remove('active-entry');
	});

	// Add active state to current entry and draw rectangles
	const tocEntry = element.closest('.toc-entry');
	if (tocEntry) {
		tocEntry.classList.add('active-entry');

		// Draw rectangles for the highlighted entry
		if (currentViewImage?.overlays && entries) {
			const entryId = tocEntry.id;
			if (entryId?.startsWith('tocEntry')) {
				const entryNumber = parseInt(entryId.replace('tocEntry', ''));
				setTimeout(() => {
					drawRectNumber(currentViewImage, entries, scaleFactor, entryNumber);
				}, 50);
			}
		}
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
	// Define overlay groups configuration
	const overlayGroups = [
		{ name: "entry", styleClass: "entry", interactive: false },
		{ name: "author", styleClass: "author", interactive: false },
		{ name: "pagenum", styleClass: "pagenum", interactive: false },
		{ name: "institution", styleClass: "institution", interactive: false },
		{ name: "title", styleClass: "title", interactive: false },
		{ name: "entry-border", styleClass: "entry-border", interactive: false },
		{ name: "author-border", styleClass: "author-border", interactive: false },
		{ name: "pagenum-border", styleClass: "pagenum-border", interactive: false },
		{ name: "institution-border", styleClass: "institution-border", interactive: false },
		{ name: "title-border", styleClass: "title-border", interactive: false }
	];

	const imageViewConfig = {
		global: {
			zoomSpeed: 1.2,
			divId: 'mainImage',
			zoomSlider: 'zoomSlider',
			maxZoomLevel: 10,
			persistZoom: true,
			persistRotation: true,
			persistenceId: getProcessId(),
			overlayGroups: overlayGroups
		},
		image: {
			mimeType: "image/jpg",
			tileSource: getTilesource(),
		}
	};
	currentViewImage = new ImageView.Image(imageViewConfig);
	currentViewImage.close();

	currentViewImage.load().then(() => {
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

		// Set up click handler for image interaction
		const mainImage = document.getElementById('mainImage');
		if (mainImage) {
			mainImage.addEventListener('click', (event) => {
				if(event.target.nodeName !== "CANVAS" || event.ctrlKey) return;

				const pixel = new OpenSeadragon.Point(event.offsetX, event.offsetY);
				const pos = currentViewImage.viewer.viewport.viewerElementToImageCoordinates(pixel);
				findAndMarkRect(entries, pos, scaleFactor);
			});
		}
	}).catch(err => console.error('Image loading error:', err));

	// Initialize click handlers for buttons with data-plugin-drawRect
	const initRectDraw = () => {
		document.removeEventListener('click', window._globalRectClickHandler, true);

		window._globalRectClickHandler = (event) => {
			const target = event.target;
			if (target?.dataset?.pluginDrawRect &&
				(target.tagName === 'A' || target.tagName === 'BUTTON')) {

				event.preventDefault();

				if (!currentViewImage?.overlays) return;

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

		document.addEventListener('click', window._globalRectClickHandler, true);
	};

	initRectDraw();
};