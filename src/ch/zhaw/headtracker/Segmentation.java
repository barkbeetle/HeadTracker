package ch.zhaw.headtracker;

import ch.zhaw.headtracker.image.Image;
import java.util.ArrayList;
import java.util.List;

public class Segmentation {
	class segment {
		/*! @brief These contain the first pixels that are and aren't part of teh object respectively. */ int begin, end;
		/*! @brief This contains a reference to the object the segment has been assigned to. */ object pObject;
	}

	/*! @brief One instance of s_objectPool holds multiple linked lists of objects which may be moved between the lists. */
	class object {
		/*! @brief Boundaries of the object, ie. all points recognized as beeing parts of the object lie within these boundaries. */ int left, right, top, bottom;
		/*! @brief Sum of the positions of all points part of this object. */ int posWghtX, posWghtY;
		/*! @brief Number of points part of this object. */ int weight;
		/*! @brief Color of the object as measured in the center of the object. */
		//s_color color;
		/*! @brief How the object has been classified. */
		//e_classification classification;
		/*! @brief Whether the object has been found to be a duplicate of an object seen in a previous image. This prevents an object from beeing counted multiple times. */ boolean isDuplicate;
		/*! @brief Pointers to adjacent elements in the linked list. */ object pPrev, pNext;
	}

	List<object> activeObjects = new ArrayList<object>();


	/*!
	 * @brief Find segments in one line of a grayscale picture.
	 *
	 * @param pImg Pointer to the first pixel of the line to find the segments in.
	 * @param value The minimum value to be considered part of a segment.
	 * @param pSegArr A pointer to the segment array the segments will be returned in.
	 */
	List<segment> findSegments(Image pImg, int value, int iy) {
		List<segment> pSegArr = new ArrayList<segment>();
		int i = 0;

		/* This loops the maximum number of segments in the segment array. */
		for (; ; ) {
			segment segment = new segment();

			/* This loops to the first point to be part of a segment. */
			while (i < pImg.width && pImg.getPixel(i, iy) < value)
				i += 1;
			segment.begin = i;
			if (i == pImg.width)
				break;

			/* This loops to the first point not part of a segment anymore. */
			while (i < pImg.width && pImg.getPixel(i, iy) >= value)
				i += 1;
			/* We ended a segment, possibly at the end of the line */
			segment.end = i;
			segment.pObject = null;
			if (i == pImg.width) {
				pSegArr.add(segment);
				break;
			}
			pSegArr.add(segment);
		}
		/* we hit the end of the line or ran out of segments */

		return pSegArr;
	}

	/*! @brief Creates an object from a single segment. */
	object createObjectForSegment(int line, segment pSeg) {
		object obj = new object();

		/* Check whether there are object left in the list of inactive objects. */
		if (obj != null) {
			/* Set the object's parameters according to the segment. */
			obj.left = pSeg.begin;
			obj.right = pSeg.end;
			obj.weight = obj.right - obj.left;
			obj.top = line;
			obj.bottom = line + 1;
			obj.posWghtX = obj.weight * (obj.left + obj.right) / 2;
			obj.posWghtY = obj.weight * line;
			obj.isDuplicate = false;

			/* Move the segment to the list of active objects and assign the segment to the object. */
			//	objectPool_move(pObjPool, obj, 0, 1);
			activeObjects.add(obj);

			pSeg.pObject = obj;
		}

		return obj;
	}

	/*! @brief Merges two objects and re-labels the segments given. */
	object mergeObjects(object pObj1, object pObj2, List<segment> pSegArr1, List<segment> pSegArr2) {
		/* Check whether we actualy have only one object in which case we just return that one. */
		if (pObj1 == null)
			return pObj2;
		else if (pObj2 == null)
			return pObj1;
		else if (pObj1 != pObj2) {	/* If the objects are not the same the second is merged into the first one. */
			int i;

			/* Here we merge the objects' parameters. */
			pObj1.left = Math.min(pObj1.left, pObj2.left);
			pObj1.right = Math.max(pObj1.right, pObj2.right);
			pObj1.weight = pObj1.weight + pObj2.weight;
			pObj1.top = Math.min(pObj1.top, pObj2.top);
			pObj1.bottom = Math.max(pObj1.bottom, pObj2.bottom);
			pObj1.posWghtX = pObj1.posWghtX + pObj2.posWghtX;
			pObj1.posWghtY = pObj1.posWghtY + pObj2.posWghtY;

			/* Relabel all segments of the merged object. */
			for (i = 0; i < pSegArr1.size(); i += 1)
				if (pSegArr1.get(i).pObject == pObj2)
					pSegArr1.get(i).pObject = pObj1;

			for (i = 0; i < pSegArr2.size(); i += 1)
				if (pSegArr2.get(i).pObject == pObj2)
					pSegArr2.get(i).pObject = pObj1;

			/* Move the merged object in to the inactive list. */
			//	objectPool_move (pObjPool, pObj2, 1, 0);
			activeObjects.remove(pObj2);
		}

		return pObj1;
	}

	/*!
		 * @brief Find objects in a picture.
		 *
		 * This function finds object in the grayscale image data.imgGrey where an object consists on pixels that are at least as bright as value.
		 *
		 * @param pPool Object pool to get the objects from.
		 * @param value Threshold to decide what is part of an object.
		 * @return A pointer to the first object in the linked list of found objects.
		 */
	List<object> findObjects(Image imgGrey, int value) {


		int i;
		int iLast, iCurrent;

		/* These two segment arrays contain the segments of the last an the current line. */
		List<segment>[] segArrs = new List[2];
		List<segment> segsLast = new ArrayList<segment>(), segsCurr = new ArrayList<segment>();

		/* Here we empty the list of objects of two images before. */
		//	while (pPool.pFirst[2] != null)
		//		objectPool_move (pPool, pPool.pFirst[2], 2, 0);

		/* And the objects of the last image to the list for the images of the last object. */
		//	pPool.pFirst[2] = pPool.pFirst[1];
		//	pPool.pFirst[1] = NULL;
		activeObjects.clear();

		//	segsCurr.numSegments = 0;

		/* this loops over every line, starting from the second */
		for (i = 0; i < imgGrey.height; i += 1) {	/* both segsLast and segsCurr point to a valid aSegment instance */
			object obj = null; /* This holds the object for the last segment processed. */

			/* swap the pointers to the last and the current segment array */
			List<segment> segmentsTemp = segsLast;
			segsLast = segsCurr;
			segsCurr = segmentsTemp;
			segsCurr = findSegments(imgGrey, value, i);

			iLast = iCurrent = 0;

			/* this loops over all segments of the last and current line */
			while (iLast < segsLast.size() || iCurrent < segsCurr.size()) {	/* both segsLast and segsCurr point to a valid aSegment instance, but iLast and iCurrent may point past the end of the array. */
				/* First we check, whether we moved on the current or on the last line the last step. */

				if (iCurrent < segsCurr.size()) {	/* There are segments on the current line. */
					if (obj == null) {	/* This means that we either moved on the current line or that we are on the first segment. So we create an object for the lower line. */
						obj = createObjectForSegment(i, segsCurr.get(iCurrent));
					}

					/* So we check whether we also have segments on the last line. */
					if (iLast < segsLast.size()) {	/* We do have segments on the last and the current line so we check if they overlap. */
						if (segsLast.get(iLast).begin < segsCurr.get(iCurrent).end && segsCurr.get(iCurrent).begin < segsLast.get(iLast).end) {	/* They do overlap so we merge the segment from the current line into the object from the segment from the last. */
							obj = mergeObjects(segsLast.get(iLast).pObject, obj, segsLast, segsCurr);

							/* We need to check which segment ends first. */
							if (segsLast.get(iLast).end < segsCurr.get(iCurrent).end) {
								iLast += 1;
							} else {
								obj = null;
								iCurrent += 1;
							}
						} else {	/* They do not overlap so we just have to create a new object for the segment of the current line. */
							/* We need to check which segment ends first. */
							if (segsLast.get(iLast).end < segsCurr.get(iCurrent).end) {	/* The segment on the last line ends first, so we just skip the segment from the last line. */
								iLast += 1;
							} else {	/* The segment on the current line ends first. So we will have to generate a new object for the next segment on the current line. */
								obj = null;
								iCurrent += 1;
							}
						}
					} else {	/* We only have segments on the current line. This menas that we have to generate a new object for the next segment. */
						obj = null;
						iCurrent += 1;
					}
				} else {	/* There are no segments on the current line left so we just skip the ones from the last line. */
					iLast += 1;
				}
			}
		}

		return new ArrayList<object>(activeObjects);
	}
}
