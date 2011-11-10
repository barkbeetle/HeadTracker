package ch.zhaw.headtracker;

import ch.zhaw.headtracker.image.Image;
import java.util.*;

public class Segmentation {
	private Segmentation() {
	}

	private static final class Segment {
		// These contain the first pixels that are and aren't part of teh object respectively.
		public final int left;
		public final int right;

		// This contains a reference to the object the segment has been assigned to.
		public Group group = null;

		public Segment(int left, int right) {
			this.left = left;
			this.right = right;
		}
	}

	public static final class Group {
		// Boundaries of the object, ie. all points recognized as being part of the object lie within these boundaries.
		public final int left;
		public final int right;
		public final int top;
		public final int bottom;

		// Sum of the positions of all points part of this object.
		public final int posSumX;
		public final int posSumY;

		// Number of points part of this object.
		public final int sum;

		private Group(Group group1, Group group2) {
			left = Math.min(group1.left, group2.left);
			right = Math.max(group1.right, group2.right);
			sum = group1.sum + group2.sum;
			top = Math.min(group1.top, group2.top);
			bottom = Math.max(group1.bottom, group2.bottom);
			posSumX = group1.posSumX + group2.posSumX;
			posSumY = group1.posSumY + group2.posSumY;
		}

		public Group(int begin, int end, int line) {
			left = begin;
			right = end;
			sum = end - begin;
			top = line;
			bottom = line + 1;
			posSumX = sum * (begin + end) / 2;
			posSumY = sum * line;
		}

		// Merges two objects and re-labels the segments given.
		static Group merge(Group group1, Group group2, List<Segment> segments1, List<Segment> segments2, Set<Group> groups) {
			// Check whether we actualy have only one object in which case we just return that one.
			if (group1 == group2)
				return group1;

			Group res = new Group(group1, group2);
			
			/* Relabel all segments of the merged object. */
			for (Segment i : segments1)
				if (i.group == group1 || i.group == group2)
					i.group = res;

			for (Segment i : segments2)
				if (i.group == group1 || i.group == group2)
					i.group = res;
			
			groups.remove(group1);
			groups.remove(group2);
			groups.add(res);

			return res;
		}
	}

	// Find segments in one line of a grayscale picture.
	private static List<Segment> findSegments(Image image, int iy) {
		List<Segment> segments = new ArrayList<Segment>();
		int i = 0;

		// This loops the maximum number of segments in the segment array.
		while (i < image.width) {
			// This loops to the first point to be part of a segment.
			while (i < image.width && image.getPixel(i, iy) != 0)
				i += 1;

			int begin = i;

			if (i == image.width)
				break;

			// This loops to the first point not part of a segment anymore.
			while (i < image.width && image.getPixel(i, iy) == 0)
				i += 1;

			// We ended a segment, possibly at the end of the line.
			segments.add(new Segment(begin, i));
		}

		return segments;
	}

	// Return a list of groups of adjacent, black pixels
	public static Set<Group> findGroups(Image image) {
		Set<Group> groups = new HashSet<Group>();
		List<Segment> segmentsBottom = new ArrayList<Segment>();

		// This loops over every line, starting from the second.
		for (int i = 0; i < image.height; i += 1) {
			// The object for the last segment processed.
			Group currentGroup = null; 
			List<Segment> segmentsTop = segmentsBottom;
			segmentsBottom = findSegments(image, i);

			int iLast = 0;
			int iCurrent = 0;

			/* this loops over all segments of the last and current line */
			while (iLast < segmentsTop.size() || iCurrent < segmentsBottom.size()) {
				// iLast and iCurrent may point past the end of the array.
				if (iCurrent < segmentsBottom.size()) {
					// There are segments on the current line.
					Segment currentSegmentBottom = segmentsBottom.get(iCurrent);
					
					if (currentGroup == null) {
						// This means that we either moved on the current line or that we are on the first segment. So we create an object for the lower line.
						currentGroup = new Group(currentSegmentBottom.left, currentSegmentBottom.right, i);

						currentSegmentBottom.group = currentGroup;
						groups.add(currentSegmentBottom.group);
					}

					// So we check whether we also have segments on the last line.
					if (iLast < segmentsTop.size()) {
						// We do have segments on the last and the current line so we check if they overlap.
						Segment currentSegmentTop = segmentsTop.get(iLast);
						
						if (currentSegmentTop.left < currentSegmentBottom.right && currentSegmentBottom.left < currentSegmentTop.right) {
							// They do overlap so we merge the segment from the current line into the object from the segment from the last.
							currentGroup = Group.merge(currentSegmentTop.group, currentGroup, segmentsTop, segmentsBottom, groups);

							// We need to check which segment ends first.
							if (currentSegmentTop.right < currentSegmentBottom.right) {
								iLast += 1;
							} else {
								currentGroup = null;
								iCurrent += 1;
							}
						} else {
							// They do not overlap so we just have to create a new object for the segment of the current line.
							// We need to check which segment ends first.
							if (currentSegmentTop.right < currentSegmentBottom.right) {	/* The segment on the last line ends first, so we just skip the segment from the last line. */
								iLast += 1;
							} else {
								// The segment on the current line ends first. So we will have to generate a new object for the next segment on the current line.
								currentGroup = null;
								iCurrent += 1;
							}
						}
					} else {
						// We only have segments on the current line. This menas that we have to generate a new object for the next segment.
						currentGroup = null;
						iCurrent += 1;
					}
				} else {
					// There are no segments on the current line left so we just skip the ones from the last line.
					iLast += 1;
				}
			}
		}

		return groups;
	}
}
