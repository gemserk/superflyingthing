package com.gemserk.games.superflyingthing.levels;

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.svg.inkscape.SvgDocument;
import com.gemserk.commons.svg.inkscape.SvgDocumentHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroup;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroupHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImageHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapePathHandler;
import com.gemserk.commons.svg.inkscape.SvgParser;
import com.gemserk.commons.svg.inkscape.SvgPath;
import com.gemserk.vecmath.Matrix3f;
import com.gemserk.vecmath.Vector2f;
import com.gemserk.vecmath.Vector3f;

public class SvgLayerProcessor {

	private SvgDocument svgDocument;
	private String layer;

	private Stack<Matrix3f> transformStack;

	public SvgLayerProcessor(String layer) {
		this.layer = layer;
		this.transformStack = new Stack<Matrix3f>();
		Matrix3f identity = new Matrix3f();
		identity.setIdentity();
		transformStack.push(identity);
	}

	public void process(Document document) {
		SvgParser svgParser = new SvgParser();
		svgParser.addHandler(new SvgDocumentHandler() {
			@Override
			protected void handle(SvgParser svgParser, SvgDocument svgDocument, Element element) {
				SvgLayerProcessor.this.svgDocument = svgDocument;
				handleDocument(svgDocument, element);
			}
		});
		svgParser.addHandler(new SvgInkscapeGroupHandler() {
			
			@Override
			protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
				if (isNotLayer(svgInkscapeGroup)) {
					svgParser.processChildren(false);
					return;
				}

				Matrix3f transform = new Matrix3f(svgInkscapeGroup.getTransform());
				transform.mul(transformStack.peek());
				transformStack.push(transform);
			}

			@Override
			protected void postHandle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
				if (isNotLayer(svgInkscapeGroup))
					return;
				transformStack.pop();
			}

			private boolean isNotLayer(SvgInkscapeGroup svgInkscapeGroup) {
				return isInkscapeLayer(svgInkscapeGroup) && !isExpectedLayer(svgInkscapeGroup);
			}

			private boolean isExpectedLayer(SvgInkscapeGroup svgInkscapeGroup) {
				return svgInkscapeGroup.getLabel().equalsIgnoreCase(layer);
			}

			private boolean isInkscapeLayer(SvgInkscapeGroup svgInkscapeGroup) {
				return svgInkscapeGroup.getGroupMode().equals("layer");
			}

		});
		svgParser.addHandler(new SvgInkscapeImageHandler() {

			Matrix3f transform = new Matrix3f();

			private boolean isFlipped(Matrix3f matrix) {
				return matrix.getM00() != matrix.getM11();
			}

			@Override
			protected void handle(SvgParser svgParser, SvgInkscapeImage svgImage, Element element) {

				if (svgImage.getLabel() == null)
					return;

				float width = svgImage.getWidth();
				float height = svgImage.getHeight();

				transform.set(svgImage.getTransform());

				// Matrix3f transform = svgImage.getTransform();

				Matrix3f groupTransform = transformStack.peek();

				transform.mul(groupTransform);

				Vector3f position = new Vector3f(svgImage.getX() + width * 0.5f, svgImage.getY() + height * 0.5f, 1f);
				transform.transform(position);

				Vector3f direction = new Vector3f(1f, 0f, 0f);
				transform.transform(direction);

				float angle = 360f - (float) (Math.atan2(direction.y, direction.x) * 180 / Math.PI);

				float sx = 1f;
				float sy = 1f;

				if (isFlipped(transform))
					sy = -1f;

				// this stuff should be processed automatically using SVG specification with transformation of the document, etc.
				float x = position.x;
				float y = svgDocument.getHeight() - position.y;

				handleImageObject(svgImage, element, x, y, width, height, sx, sy, angle);
			}

		});
		svgParser.addHandler(new SvgInkscapePathHandler() {

			Vector3f tmp = new Vector3f();

			@Override
			protected void handle(SvgParser svgParser, SvgPath svgPath, Element element) {
				Vector2f[] points = svgPath.getPoints();
				Vector2[] vertices = new Vector2[points.length];

				Matrix3f transform = transformStack.peek();

				for (int i = 0; i < points.length; i++) {
					Vector2f point = points[i];

					tmp.set(point.x, point.y, 1f);
					transform.transform(tmp);

					point.set(tmp.x, tmp.y);

					// this coordinates transform, should be processed when parsed
					vertices[i] = new Vector2(point.x, svgDocument.getHeight() - point.y);
				}
				handlePathObject(svgPath, element, vertices);
			}
		});
		svgParser.parse(document);
	}

	protected void handleDocument(SvgDocument document, Element element) {

	}

	protected void handlePathObject(SvgPath svgPath, Element element, Vector2[] vertices) {

	}

	protected void handleImageObject(SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {

	}

}