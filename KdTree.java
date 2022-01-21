/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;

public class KdTree {
    private Node root;


    private static class Node {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        private int size;
        private boolean vertical;

        public Node(Point2D point, boolean orientation, int size, RectHV rect) {
            // RectHV rect
            this.p = point;
            this.rect = rect;
            this.vertical = orientation;
            this.size = size;

        }


    }


    // construct an empty set of points
    public KdTree() {

    }

    //  private boolean getOrientation(Node x) {

    //       if (Math.log(size()) % 2 == 0)
    //          return true;
    //       else return false;
    //  }

    // is the set empty?
    public boolean isEmpty() {
        return size() == 0;
    }

    // number of points in the set
    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) return 0;
        else return x.size;
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument to insert() is null");
        root = insert(root, p, true, new RectHV(0, 0, 1, 1));

    }

    private Node insert(Node x, Point2D point, boolean orient, RectHV rectangle) {


        if (x == null)
            return new Node(point, orient, 1, rectangle);

        double cmp;
        RectHV rectLeft = null;
        RectHV rectRight = null;

        if (x.vertical) {
            cmp = Point2D.X_ORDER.compare(point, x.p);
            if (cmp < 0 && x.lb == null)
                rectLeft = new RectHV(x.rect.xmin(), x.rect.ymin(), x.p.x(), x.rect.ymax());

            if (cmp >= 0 && x.rt == null)
                rectRight = new RectHV(x.p.x(), x.rect.ymin(), x.rect.xmax(), x.rect.ymax());

        }
        else {
            cmp = Point2D.Y_ORDER.compare(point, x.p);
            if (cmp < 0 && x.lb == null)
                rectLeft = new RectHV(x.rect.xmin(), x.rect.ymin(), x.rect.xmax(), x.p.y());
            if (cmp >= 0 && x.rt == null)
                rectRight = new RectHV(x.rect.xmin(), x.p.y(), x.rect.xmax(), x.rect.ymax());

        }


        if (cmp < 0) x.lb = insert(x.lb, point, !orient, rectLeft);
        else if (cmp > 0) x.rt = insert(x.rt, point, !orient, rectRight);
        else if (!point.equals(x.p)) x.rt = insert(x.rt, point, !orient, rectRight);

        x.size = 1 + size(x.lb) + size(x.rt);

        return x;


    }


    // does the set contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument to contains() is null");
        return contains(root, p) != null;


    }

    private Node contains(Node x, Point2D point) {
        if (point == null) throw new IllegalArgumentException("calls get() with a null key");
        if (x == null) return null;
        double cmp;
        if (x.vertical)
            cmp = Point2D.X_ORDER.compare(point, x.p);
        else
            cmp = Point2D.Y_ORDER.compare(point, x.p);

        if (cmp < 0) return contains(x.lb, point);
        if (cmp > 0) return contains(x.rt, point);
        else if (!point.equals(x.p)) return contains(x.rt, point);
        else return x;

    }

    // draw all points to standard draw
    public void draw() {
        draw(root, root.p.x(), root.rect.ymin(), root.p.x(), root.rect.ymax());

    }

    private void draw(Node x, double X1, double Y1, double X2, double Y2) {

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
        x.p.draw();
        if (x.vertical) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius();
            StdDraw.line(X1, Y1, X2, Y2);
            if (x.lb != null)
                draw(x.lb, x.lb.rect.xmin(), x.lb.p.y(), X2, x.lb.p.y());
            if (x.rt != null)
                draw(x.rt, X2, x.rt.p.y(), x.rect.xmax(), x.rt.p.y());
        }
        else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius();
            StdDraw.line(X1, Y1, X2, Y2);
            if (x.lb != null)
                draw(x.lb, x.lb.p.x(), x.lb.rect.ymin(), x.lb.p.x(), Y1);
            if (x.rt != null)
                draw(x.rt, x.rt.p.x(), Y1, x.rt.p.x(), x.rt.rect.ymax());
        }


    }


    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException();

        Stack<Point2D> stack = new Stack<Point2D>();
        range(root, rect, stack);
        return stack;

    }

    private void range(Node x, RectHV rect, Stack<Point2D> stackie) {
        if (x == null) return;
        if (rect.contains(x.p))
            stackie.push(x.p);
        if (x.lb != null) {
            if (x.lb.rect.intersects(rect))
                range(x.lb, rect, stackie);
        }
        if (x.rt != null) {
            if (x.rt.rect.intersects(rect))
                range(x.rt, rect, stackie);
        }

    }


    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        return nearest(root, p, root.p);
    }

    private Point2D nearest(Node x, Point2D queryPoint, Point2D champion) {
        double dist = champion.distanceSquaredTo(queryPoint);
        double fakeDist = x.p.distanceSquaredTo(queryPoint);
        if (fakeDist < dist)
            champion = x.p;

        
        if (x.lb != null) {
            if (champion.distanceSquaredTo(queryPoint) > x.lb.rect
                    .distanceSquaredTo(queryPoint))
                champion = nearest(x.lb, queryPoint, champion);
        }
        if (x.rt != null) {
            if (champion.distanceSquaredTo(queryPoint) > x.rt.rect
                    .distanceSquaredTo(queryPoint))
                champion = nearest(x.rt, queryPoint, champion);
        }

        return champion;


    }

    // unit testing of the methods (optional)
    public static void main(String[] args) {

    }
}
