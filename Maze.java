import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;



//a vertex in a graph
class Vertex {
  int x;
  int y;
  boolean visited;
  boolean path;
  ArrayList<Edge> edges;

  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
    this.edges = new ArrayList<Edge>();
    this.visited = false;
    this.path = false;
  }

  // uniquely identifies vertices
  public int hashCode() {
    return 1000 * y + x;
  }

  //checks equality of vertices
  public boolean equals(Object other) {
    if (!(other instanceof Vertex)) { 
      return false; 
    }
    Vertex that = (Vertex)other;
    return this.edges.equals(that.edges)
        && this.x == that.x
        && this.y == that.y
        && this.visited == that.visited
        && this.path == that.path;
  }
}

//an edge in a graph
class Edge {
  Vertex from;
  Vertex to;
  int weight;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

}




//our game world
class Maze extends World {
  // width of the world in cells
  static final int WIDTH = 30;
  // height of the world in cells
  static final int HEIGHT = 50;
  // scale of the world
  static final int SCALE = 10;
  //all vertices
  ArrayList<Vertex> vertices;
  //all walls
  ArrayList<Edge> walls;
  //different search methods
  //breadth first search
  boolean bfs;
  BreadthFirst b;
  //depth first search
  boolean dfs;
  DepthFirst d;
  //manual player search
  boolean player;
  Player p;

  //constructs a maze
  Maze() {
    init();
  }

  // sets up our maze by constructing the vertices, walls and initializing
  // our variables
  void init() {
    ArrayList<ArrayList<Vertex>> v = initVertices();
    ArrayList<Edge> allEdges = allEdges(v);
    v = kruskal(v);
    walls = walls(v, allEdges);
    vertices = new ArrayList<Vertex>();
    for (ArrayList<Vertex> vList : v) {
      for (Vertex vt : vList) {
        vertices.add(vt);
      }
    }
    bfs = false;
    dfs = false;
    player = false;
    b = new BreadthFirst(vertices);
    d = new DepthFirst(vertices);
    p = new Player(vertices);
  }

  // makes the walls for our maze
  ArrayList<Edge> walls(ArrayList<ArrayList<Vertex>> v, ArrayList<Edge> all) {
    ArrayList<Edge> w = new ArrayList<Edge>();
    for (Edge e : all) {
      boolean valid = true;
      for (ArrayList<Vertex> l : v) {
        for (Vertex vt : l) {
          for (Edge e2 : vt.edges) {
            if (e.equals(e2) || (e.to == e2.from && e.from == e2.to)) {
              valid = false;
            }
          }
        }
      }
      if (valid) {
        w.add(e);
      }
    }
    return w;
  }

  // creates vertices
  ArrayList<ArrayList<Vertex>> initVertices() {
    ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
    for (int x = 0; x < WIDTH; x++) {
      ArrayList<Vertex> vertices2 = new ArrayList<Vertex>();
      for (int y = 0; y < HEIGHT; y++) {
        vertices2.add(new Vertex(x, y));
      }
      vertices.add(vertices2);
    }
    Random r = new Random();
    for (ArrayList<Vertex> col : vertices) {
      for (Vertex v : col) {
        if (v.x != 0) {
          v.edges.add(new Edge(v, vertices.get(v.x - 1).get(v.y), r.nextInt(1000)));
        }
        if (v.x != WIDTH - 1) {
          v.edges.add(new Edge(v, vertices.get(v.x + 1).get(v.y), r.nextInt(1000)));
        }
        if (v.y != 0) {
          v.edges.add(new Edge(v, vertices.get(v.x).get(v.y - 1), r.nextInt(1000)));
        }
        if (v.y != HEIGHT - 1) {
          v.edges.add(new Edge(v, vertices.get(v.x).get(v.y + 1), r.nextInt(1000)));
        }
      }
    }
    return vertices;
  }  

  //returns an arraylist of every edge in a list of vertices
  ArrayList<Edge> allEdges(ArrayList<ArrayList<Vertex>> v) {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    for (ArrayList<Vertex> cols : v) {
      for (Vertex vert : cols) {
        for (Edge edge : vert.edges) {
          edges.add(edge);
        }
      }
    }
    return edges;
  }

  //implementation of krsuskals algorithm to create maze
  ArrayList<ArrayList<Vertex>> kruskal(ArrayList<ArrayList<Vertex>> v) {
    ArrayList<Edge> allEdges = allEdges(v);
    for (ArrayList<Vertex> i : v) {
      for (Vertex j : i) {
        j.edges = new ArrayList<Edge>();
      }
    }
    int total = HEIGHT * WIDTH;
    ArrayList<Edge> worklist = new ArrayList<Edge>();
    ArrayList<Edge> sortedEdges = sort(allEdges, new EdgesByWeight());
    HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
    for (int i = 0; i <= (1000 * HEIGHT) + WIDTH; i++) {
      hash.put(i, i);
    }
    ArrayList<Edge> l = sortedEdges;
    while (worklist.size() < total - 1) {
      Edge e = l.get(0);
      if (this.find(hash, e.to.hashCode()) != this.find(hash, e.from.hashCode())) {
        worklist.add(e);
        e.from.edges.add(e);
        e.to.edges.add(new Edge(e.to, e.from, e.weight));
        int temp = (find(hash, e.to.hashCode()));
        hash.remove(find(hash, e.to.hashCode()));
        hash.put(temp, find(hash, e.from.hashCode()));
      }
      l.remove(0);
    }
    return v;
  }

  // implements the find part of kurskals algorithm
  int find(HashMap<Integer, Integer> hash, int identity) {
    if (hash.get(identity) == identity) {
      return identity;
    }
    else {
      return find(hash, hash.get(identity));
    }
  }

  //sorts an array of edges
  public ArrayList<Edge> sort(ArrayList<Edge> allEdges, EdgesByWeight compare) {
    ArrayList<Edge> sortEdges = new ArrayList<Edge>();
    for (Edge e : allEdges) {
      sortEdges.add(e);
    }
    sortEdges.sort(compare);
    return sortEdges;
  }


  // generates an appropriate color given a vertex
  Color color(Vertex v) {
    if (v.x == WIDTH - 1 && v.y == HEIGHT - 1) {
      return Color.green;
    }
    else if (v.path) {
      return Color.blue;
    }
    else if (v.x == 0 && v.y == 0) {
      return Color.red;
    }
    else if (v.visited) {
      return Color.cyan;
    }
    else {
      return Color.gray;
    }
  }

  //solve and movement based on keys pressed
  public void onKeyEvent(String k) {
    if (k.equals("b")) {
      bfs = true;
      dfs = false;
      player = false;
      reset();
    }
    if (k.equals("d")) {
      bfs = false;
      dfs = true;
      player = false;
      reset();
    }
    if (k.equals("p")) {
      bfs = false;
      dfs = false;
      player = true;
      reset();
    }
    else if (k.equals("r")) {
      init();
    }
    else if (player) {
      if(p.hasNext()) {
        if (k.equals("up")) {
          p.upward();
        }
        if (k.equals("down")) {
          p.down();
        }
        if (k.equals("left")) {
          p.left();
        }
        if (k.equals("right")) {
          p.right();
        }
      }
    }
  }

  //advances bfs/dfs on every tick
  public void onTick() {
    if (bfs) {
      if (b.hasNext()) {
        b.next();
      }
    }
    if (dfs) {
      if (d.hasNext()) {
        d.next();
      }
    }
  }

  //resets the game
  public void reset() {
    for (Vertex v : vertices) {
      v.path = false;
      v.visited = false;
    }
    b = new BreadthFirst(vertices);
    d = new DepthFirst(vertices);
    p = new Player(vertices);
  }

  @Override
  //draws the world scene
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(WIDTH * SCALE, HEIGHT * SCALE);
    for (Vertex v : vertices) {
      Color col = color(v);
      w.placeImageXY(new RectangleImage(SCALE, SCALE, OutlineMode.SOLID, col),
          (v.x * SCALE) + (SCALE * 1 / 2), (v.y * SCALE) + (SCALE * 1 / 2));
    }
    for (Edge e : walls) {
      if (e.to.x == e.from.x) {
        w.placeImageXY(
            new RectangleImage(SCALE, SCALE / 10, OutlineMode.SOLID, Color.black),
            (e.to.x * SCALE) + (SCALE * 1 / 2),
            ((e.to.y + e.from.y) * SCALE / 2) + (SCALE * 1 / 2));
      }
      else {
        w.placeImageXY(
            new RectangleImage(SCALE / 10, SCALE, OutlineMode.SOLID, Color.black),
            ((e.to.x + e.from.x) * SCALE / 2) + (SCALE * 1 / 2),
            (e.to.y * SCALE) + (SCALE * 1 / 2));
      }
    }
    return w;
  }

}

//abstract class that represent search methods
abstract class Search {
  //keeps track of path
  HashMap<Integer, Vertex> path;

  //constructs path that represents solution
  void reconstruct(HashMap<Integer, Vertex> hash, Vertex next) {
    while (hash.containsKey(next.hashCode())) {
      next.path = true;
      next = hash.get(next.hashCode());
    }
  }
}

//represents breadthfirst search
class BreadthFirst extends Search {
  //represents the worklist
  LinkedList<Vertex> worklist;

  BreadthFirst(ArrayList<Vertex> list) {
    this.worklist = new LinkedList<Vertex>();
    worklist.add(list.get(0));
    list.get(0).visited = true;
    path = new HashMap<Integer, Vertex>();
  }

  //goes through the next step of breadthfirst search
  public LinkedList<Vertex> next() {
    Vertex u = worklist.remove();
    for (Edge e : u.edges) {
      if (!e.to.visited) {
        path.put(e.to.hashCode(), e.from);
        if (e.to.x == Maze.WIDTH - 1 && e.to.y == Maze.HEIGHT - 1) {
          reconstruct(path, e.to);
          worklist = new LinkedList<Vertex>();
        }
        else {
          e.to.visited = true;
          worklist.add(e.to);
        }
      }
    }
    return worklist;
  }

  //determines if there is next in search
  public boolean hasNext() {
    return !worklist.isEmpty();
  }


}

//represents Depthfirst search
class DepthFirst extends Search {
  //our current worklist
  Stack<Vertex> worklist;

  DepthFirst(ArrayList<Vertex> list) {
    this.worklist = new Stack<Vertex>();
    worklist.push(list.get(0));
    list.get(0).visited = true;
    path = new HashMap<Integer, Vertex>();
  }

  //determines the next step of depthfirst search
  public Stack<Vertex> next() {
    Vertex u = worklist.pop();
    for (Edge e : u.edges) {
      if (!e.to.visited) {
        path.put(e.to.hashCode(), e.from);
        if (e.to.x == Maze.WIDTH - 1 && e.to.y == Maze.HEIGHT - 1) {
          reconstruct(path, e.to);
          worklist = new Stack<Vertex>();
        }
        else {
          worklist.push(u);
          e.to.visited = true;
          worklist.push(e.to);
          break;
        }
      }
    }
    return worklist;
  }
  //determiens if there is next in search
  public boolean hasNext() {
    return !worklist.isEmpty();
  }


}

//represents manual player search
class Player extends Search {
  //current vertex
  Vertex current;
  //maze finished?
  boolean finished;

  Player(ArrayList<Vertex> list) {
    current = list.get(0);
    path = new HashMap<Integer, Vertex>();
    finished = false;
  }

  //is the maze finished?
  public boolean hasNext() {
    return !finished;
  }

  //determines how to move player
  public Vertex move(boolean b, Edge e) {
    if (b) {
      current.visited = true;
      current.path = false;
      if (!e.to.visited) {
        path.put(e.to.hashCode(), e.from);
      }
      if (e.to.x == Maze.WIDTH - 1 && e.to.y == Maze.HEIGHT - 1) {
        reconstruct(path, e.to);
      }
      else {
        current = e.to;
        current.path = true;
      }
    }
    return current;
  }

  //moves position left and adds
  //each path to table
  public Vertex left() {
    for (Edge e : current.edges) {
      move(e.to.x == current.x - 1, e);
    }
    return current;
  }

  //moves position right and adds
  //each path to table
  public Vertex right() {
    for (Edge e : current.edges) {
      move(e.to.x == current.x + 1, e);
    }
    return current;
  }

  //moves position down and adds
  //each path to table
  public Vertex down() {
    for (Edge e : current.edges) {
      move(e.to.y == current.y + 1, e);
    }
    return current;
  }

  //moves position up and adds
  //each path to table
  public Vertex upward() {
    for (Edge e : current.edges) {
      move(e.to.y == current.y - 1, e);
    }
    return current;
  }

  //constructs the true path
  void reconstruct(HashMap<Integer, Vertex> h, Vertex next) {
    while (h.containsKey(next.hashCode())) {
      next.path = true;
      next = h.get(next.hashCode());
    }
  }
}


//compares Edges by weight
class EdgesByWeight implements Comparator<Edge> {

  @Override
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}


//examples class
class ExamplesMaze {
  EdgesByWeight comparator;
  Vertex v1;
  Vertex v2;
  Vertex v3;
  Edge e1;
  Edge e2;
  Edge e3;
  Maze m1;

  //init data
  void initData() {
    comparator = new EdgesByWeight();
    v1 = new Vertex(0, 0);
    v2 = new Vertex(0, 0);
    v3 = new Vertex(1, 0);
    e1 = new Edge(v1 ,v3, 2);
    e2 = new Edge(v3, v1, 5);
    e3 = new Edge(v2, v2, 2);
    m1 = new Maze();
  }

  //tests hashcode
  void testHashcode(Tester t) {
    this.initData();
    t.checkExpect(v1.hashCode(), 0);
    t.checkExpect(v3.hashCode(), 1);
  }

  //tests equals
  void testEquals(Tester t) {
    this.initData();
    t.checkExpect(v1.equals(v1), true);
    t.checkExpect(v2.equals(v2), true);
    t.checkExpect(v3.equals(v1), false);

  }

  //tests sort method
  void testSort(Tester t) {
    this.initData();
    ArrayList<Edge> empty = new ArrayList<Edge>();
    t.checkExpect(m1.sort(empty, comparator), empty);
    ArrayList<Edge> l1 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3));
    t.checkExpect(m1.sort(l1, comparator), new ArrayList<Edge>(Arrays.asList(e1, e3, e2)));
  }


  //tests compare method
  void testCompare(Tester t) {
    this.initData();
    t.checkExpect(comparator.compare(e1, e2), -3);
    t.checkExpect(comparator.compare(e2, e1), 3);
    t.checkExpect(comparator.compare(e3, e1), 0);
  }

  //tests color method
  void testColor(Tester t) {
    this.initData();
    t.checkExpect(m1.color(v1), Color.red);
    t.checkExpect(m1.color(v3), Color.gray);
    t.checkExpect(m1.color(new Vertex(m1.WIDTH - 1, m1.HEIGHT - 1)), Color.green);

  }

  //tests initVertices method
  void testInitVertices(Tester t) {
    this.initData();
    t.checkExpect(m1.initVertices().size(), m1.WIDTH);
    t.checkExpect(m1.initVertices().get(0).size(), m1.HEIGHT);
    t.checkExpect(m1.initVertices().get(0).get(0).edges.size(), 2);
    t.checkExpect(m1.initVertices().get(0).get(2).edges.size(), 3);
    t.checkExpect(m1.initVertices().get(3).get(4).edges.size(), 4);
  }

  //tests allEdges method
  void testAllEdges(Tester t) {
    this.initData();
    t.checkExpect(m1.allEdges(m1.initVertices()).size(), 
        m1.WIDTH * m1.HEIGHT * 4 - (m1.WIDTH + m1.HEIGHT) * 2);

  }

  //tests kruskal method
  void testKruskal(Tester t) {
    this.initData();
    t.checkExpect(m1.kruskal(m1.initVertices()).size(), m1.initVertices().size());
    ArrayList<Edge> edges = new ArrayList<Edge>();
    for (ArrayList<Vertex> list: m1.kruskal(m1.initVertices())) {
      for (Vertex v: list) {
        for (Edge e: v.edges) {
          if (!e.to.edges.contains(e)) {
            edges.add(e);
          }
        }
      }
    }
    t.checkExpect(edges.size(), m1.HEIGHT * m1.WIDTH * 2 - 2);
  }

  //tests walls method
  void testWalls(Tester t) {
    this.initData();
    ArrayList<ArrayList<Vertex>> v = m1.initVertices();
    ArrayList<Edge> allEdges = m1.allEdges(v);
    v = m1.kruskal(v);
    m1.walls = m1.walls(v, allEdges);
    t.checkExpect(m1.walls.size(), allEdges.size() - (m1.HEIGHT * m1.WIDTH * 2 - 2));
  }

  //tests init method
  //other methods are tested in other tests
  void testInit(Tester t) {
    this.initData();
    m1.init();
    t.checkExpect(m1.vertices.size(), m1.HEIGHT * m1.WIDTH);
  }

  //tests makeScene method
  void testMakeScene(Tester t) {
    this.initData();
    m1.init();
    WorldScene w = new WorldScene(m1.WIDTH * m1.SCALE, m1.HEIGHT * m1.SCALE);
    for (Vertex v : m1.vertices) {
      Color col = m1.color(v);
      w.placeImageXY(new RectangleImage(m1.SCALE, m1.SCALE, OutlineMode.SOLID, col),
          (v.x * m1.SCALE) + (m1.SCALE * 1 / 2), (v.y * m1.SCALE) + (m1.SCALE * 1 / 2));
    }
    for (Edge e : m1.walls) {
      if (e.to.x == e.from.x) {
        w.placeImageXY(
            new RectangleImage(m1.SCALE, m1.SCALE / 10, OutlineMode.SOLID, Color.black),
            (e.to.x * m1.SCALE) + (m1.SCALE * 1 / 2),
            ((e.to.y + e.from.y) * m1.SCALE / 2) + (m1.SCALE * 1 / 2));
      }
      else {
        w.placeImageXY(
            new RectangleImage(m1.SCALE / 10, m1.SCALE, OutlineMode.SOLID, Color.black),
            ((e.to.x + e.from.x) * m1.SCALE / 2) + (m1.SCALE * 1 / 2),
            (e.to.y * m1.SCALE) + (m1.SCALE * 1 / 2));
      }
    }
    t.checkExpect(m1.makeScene(), w);
  }
  
  //test hasNext methods
  
  //tests next methods
  
  //tests reconstruct methods
  
  //tests onTick
  
  //tests reset
  
  //

  // run the game
  void testGame(Tester t) {
    Maze m = new Maze();
    m.bigBang(Maze.WIDTH * Maze.SCALE,
        Maze.HEIGHT * Maze.SCALE, 0.005);
  }
}