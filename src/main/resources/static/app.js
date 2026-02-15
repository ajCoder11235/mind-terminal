const input = document.getElementById('commandInput');
const history = document.getElementById('history');

input.addEventListener('keydown', async function(event) {
    if (event.key === 'Enter') {
        const command = input.value.trim();
        if (command) {
            printOutput(`usr@mind:~$ ${command}`);
            await processCommand(command);
        }
        input.value = '';
    }
});

function printOutput(text) {
    const div = document.createElement('div');
    div.textContent = text;
    history.appendChild(div);
    history.scrollTop = history.scrollHeight;
}

async function processCommand(rawCmd) {
    const args = rawCmd.split(' ');
    const action = args[0].toLowerCase();

    switch(action) {
        case 'help':
            printOutput("Commands:");
            printOutput("  add <Topic>                  -> Add to Root");
            printOutput("  add <Topic> under <Parent>   -> Add sub-topic");
            printOutput("  move <Topic> under <Parent>  -> Move topic");
            printOutput("  delete <Topic>               -> Delete topic (+ sub-topics)");
            printOutput("  list                         -> Show text tree");
            printOutput("  graph                        -> Show visual map");
            break;

        case 'list':
            // ... (Keep your existing list logic) ...
             try {
                printOutput("Fetching memory map...");
                const res = await fetch('/api/nodes');
                if (res.status === 401) { printOutput("Error: Unauthorized."); return; }
                const nodes = await res.json();
                if (nodes.length === 0) printOutput("Mind Map is empty.");
                else printOutput(renderTextTree(nodes, null, 0));
            } catch (e) { printOutput("Error: Fetch failed."); }
            break;

        case 'graph':
             // ... (Keep your existing graph logic) ...
             try {
                const res = await fetch('/api/nodes');
                const data = await res.json();
                showVisualGraph(data);
            } catch (e) { printOutput("Error loading graph."); }
            break;

        case 'add':
            // ... (Keep your existing add logic) ...
            let addTopic = args.slice(1).join(' ');
            let addParent = null;
            if (addTopic.includes(' under ')) [addTopic, addParent] = addTopic.split(' under ');
            addTopic = addTopic.replace(/"/g, '').trim();
            if(addParent) addParent = addParent.replace(/"/g, '').trim();

            try {
                let url = `/api/add?label=${encodeURIComponent(addTopic)}`;
                if (addParent) url += `&parentLabel=${encodeURIComponent(addParent)}`;
                const res = await fetch(url, { method: 'POST' });
                printOutput(await res.text());
            } catch (e) { printOutput("Error: Connection failed."); }
            break;

        // --- NEW COMMAND: DELETE ---
        case 'delete':
            // Syntax: delete "Topic Name"
            let delTopic = args.slice(1).join(' ').replace(/"/g, '').trim();
            if (!delTopic) { printOutput("Usage: delete <Topic>"); break; }

            try {
                const res = await fetch(`/api/delete?label=${encodeURIComponent(delTopic)}`, { method: 'DELETE' });
                printOutput(await res.text());
            } catch (e) {
                printOutput("Error: Could not delete.");
            }
            break;

        // --- NEW COMMAND: MOVE ---
        case 'move':
            // Syntax: move "Topic" under "New Parent"
            let moveCmd = args.slice(1).join(' ');
            if (!moveCmd.includes(' under ')) {
                printOutput("Usage: move <Topic> under <NewParent>");
                break;
            }

            let [moveTopic, moveParent] = moveCmd.split(' under ');
            moveTopic = moveTopic.replace(/"/g, '').trim();
            moveParent = moveParent.replace(/"/g, '').trim();

            try {
                const url = `/api/move?label=${encodeURIComponent(moveTopic)}&newParentLabel=${encodeURIComponent(moveParent)}`;
                const res = await fetch(url, { method: 'PUT' });
                printOutput(await res.text());
            } catch (e) {
                printOutput("Error: Could not move.");
            }
            break;

        case 'clear':
            history.innerHTML = '';
            break;

        default:
            printOutput(`Error: Command '${action}' not found.`);
    }
}

function showGraph(mindMap) {
    document.getElementById('graph-container').style.display = 'block';
    // Convert backend data to Vis.js format
    const nodes = new vis.DataSet(mindMap.map(n => ({
        id: n.id, label: n.label, shape: 'box', color: '#00ff41', font: { color: 'white' }
    })));
    const edges = new vis.DataSet(mindMap.filter(n => n.parentId).map(n => ({
        from: n.parentId, to: n.id
    })));
    new vis.Network(document.getElementById('network'), { nodes, edges }, {});
}