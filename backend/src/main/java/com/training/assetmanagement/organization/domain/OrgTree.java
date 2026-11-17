package com.training.assetmanagement.organization.domain;

import java.text.Collator;
import java.util.*;

// Cây đơn vị bất biến, dựng từ một lần nạp - thuần, không I/O.
// Không cache qua request: Đổi cha / ngừng hoạt động có hiệu lực ở request kế tiếp
public final class OrgTree {

    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    private final OrgNode root;
    private final Map<UUID, OrgNode> byId;
    private final Map<UUID, List<OrgNode>> childrenById;
    private final Map<UUID, Integer> depthById;
    private final List<OrgTreeEntry> preOrder;

    private OrgTree(Collection<OrgNode> nodes) {
        Map<UUID, OrgNode> nodeMap = new HashMap<>();
        OrgNode foundRoot = null;
        for (OrgNode node : nodes) {
            nodeMap.put(node.id(), node);
            if (node.isRoot()) {
                if (foundRoot != null) {
                    throw new IllegalStateException("Org tree has more than one root");
                }
                foundRoot = node;
            }
        }
        if (foundRoot == null) {
            throw new IllegalStateException("Org tree has not root");
        }

        // Anh em sắp theo tên tiếng Việt, trùng tên thì theo mã
        Comparator<OrgNode> byName = Comparator.comparing(OrgNode::name, Collator.getInstance(VIETNAMESE))
            .thenComparing(OrgNode::code);
        Map<UUID, List<OrgNode>> children = new HashMap<>();
        for (OrgNode node : nodes) {
            if (node.isRoot()) {
                continue;
            }
            if (!nodeMap.containsKey(node.parentId())) {
                throw new IllegalStateException("Org unit " + node.id() + " has missing parent " + node.parentId());
            }
            children.computeIfAbsent(node.parentId(), key -> new ArrayList<>()).add(node);
        }
        Map<UUID, List<OrgNode>> sortedChildren = new HashMap<>();
        children.forEach((parentId, list) -> {
            list.sort(byName);
            sortedChildren.put(parentId, List.copyOf(list));
        });

        this.root = foundRoot;
        this.byId = Map.copyOf(nodeMap);
        this.childrenById = Map.copyOf(sortedChildren);

        // Duyệt từ gốc, có tập đã thăm: Dữ liệu vòng / mồ côi (chỉ có thể do SQL tay) không làm treo
        List<OrgTreeEntry> entries = new ArrayList<>(nodeMap.size());
        Map<UUID, Integer> depths = new HashMap<>();
        Set<UUID> visited = new HashSet<>();
        Deque<OrgTreeEntry> stack = new ArrayDeque<>();
        stack.push(new OrgTreeEntry(foundRoot, 0));
        while (!stack.isEmpty()) {
            OrgTreeEntry entry = stack.pop();
            if (!visited.add(entry.node().id())) {
                throw new IllegalStateException("Org tree contains a cycle at " + entry.node().id());
            }
            entries.add(entry);
            depths.put(entry.node().id(), entry.depth());
            List<OrgNode> kids = this.childrenById.getOrDefault(entry.node().id(), List.of());
            for (int i = kids.size() - 1; i >= 0; i--) {
                stack.push(new OrgTreeEntry(kids.get(i), entry.depth() + 1));
            }
        }
        // Nút không tới được từ gôốc = nằm trên một vòng
        if (entries.size() != nodeMap.size()) {
            throw new IllegalStateException("Org tree contains a cycle or detached units");
        }
        this.preOrder = List.copyOf(entries);
        this.depthById = Map.copyOf(depths);
    }

    public static OrgTree of(Collection<OrgNode> nodes) {
        return new OrgTree(nodes);
    }

    public OrgNode root() {
        return root;
    }

    public Collection<OrgNode> nodes() {
        return byId.values();
    }

    public boolean contains(UUID id) {
        return id != null && byId.containsKey(id);
    }

    public Optional<OrgNode> find(UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    public List<OrgNode> children(UUID id) {
        return childrenById.getOrDefault(id, List.of());
    }

    public int depthOf(UUID id) {
        Integer depth = depthById.get(id);
        if (depth == null) {
            throw new IllegalArgumentException("Unknown org unit " + id);
        }
        return depth;
    }

    // Gồm chính nó
    public Set<UUID> subtreeIds(UUID id) {
        Set<UUID> result = new LinkedHashSet<>();
        if (!contains(id)) {
            return result;
        }
        Deque<UUID> stack = new ArrayDeque<>();
        stack.push(id);
        while (!stack.isEmpty()) {
            UUID current = stack.pop();
            result.add(current);
            children(current).forEach(child -> stack.push(child.id()));
        }
        return result;
    }

    // Từ cha lên tới gốc, không gồm chính nó
    public List<OrgNode> ancestors(UUID id) {
        List<OrgNode> result = new ArrayList<>();
        OrgNode current = byId.get(id);
        while (current != null && current.parentId() != null) {
            current = byId.get(current.parentId());
            if (current != null) {
                result.add(current);
            }
        }
        return result;
    }

    // candidateId là unitId hoặc con cháu của unitId
    public boolean isInSubtree(UUID candidateId, UUID unitId) {
        OrgNode current = candidateId == null ? null : byId.get(candidateId);
        while (current != null) {
            if (current.id().equals(unitId)) {
                return true;
            }
            current = current.parentId() == null ? null : byId.get(current.parentId());
        }
        return false;
    }

    // Chỉ nhánh chứa đơn vị: Chính nó hoặc tổ tiên gần nhất loại BRANCH
    public Optional<OrgNode> branchOf(UUID id) {
        OrgNode current = byId.get(id);
        while (current != null) {
            if (current.unitType() == OrgUnitType.BRANCH) {
                return Optional.of(current);
            }
            current = current.parentId() == null ? null : byId.get(current.parentId());
        }
        return Optional.empty();
    }

    // Cha trước con, anh em theo tên - thứ tự trả cho FE
    public List<OrgTreeEntry> preOrder() {
        return preOrder;
    }
}
